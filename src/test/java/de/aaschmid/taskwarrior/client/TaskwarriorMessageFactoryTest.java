package de.aaschmid.taskwarrior.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.aaschmid.taskwarrior.message.TaskwarriorMessage;
import de.aaschmid.taskwarrior.message.TaskwarriorMessageDeserializationException;
import de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader;
import org.junit.jupiter.api.Test;

import static de.aaschmid.taskwarrior.client.TaskwarriorMessageFactory.deserialize;
import static de.aaschmid.taskwarrior.client.TaskwarriorMessageFactory.serialize;
import static de.aaschmid.taskwarrior.message.TaskwarriorAuthentication.taskwarriorAuthentication;
import static de.aaschmid.taskwarrior.message.TaskwarriorMessage.taskwarriorMessage;
import static de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader.taskwarriorRequestHeaderBuilder;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

class TaskwarriorMessageFactoryTest {

    private final UUID uuid = UUID.randomUUID();

    @Test
    void serialize_shouldReturnCorrectBytesArray() {
        TaskwarriorRequestHeader taskwarriorRequestHeader = taskwarriorRequestHeaderBuilder()
                .authentication(taskwarriorAuthentication(uuid, "org", "user"))
                .type(TaskwarriorRequestHeader.MessageType.SYNC)
                .client("test v0.9")
                .build();
        String payload = "This is the expected payload.";

        byte[] actual = serialize(taskwarriorMessage(taskwarriorRequestHeader.toMap(), payload));

        assertThat(actual)
                .startsWith(0, 0, 0, actual.length)
                .endsWith(("protocol: v1\norg: org\nclient: test v0.9\ntype: sync\nuser: user\nkey: " + uuid + "\n\nThis is the expected payload.")
                        .getBytes(UTF_8));
    }

    @Test
    void serialize_shouldReturnCorrectMessageLengthInResultingBytesArray() {
        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 0; i < 100; i++) {
            headers.put(format("h%03d", i), "val");
        }
        List<String> lines = new ArrayList<>();
        for (int i = 0; i <= 10_000; i++) {
            lines.add(format("%099d", i));
        }

        byte[] actual = serialize(taskwarriorMessage(headers, join("\n", lines)));

        assertThat(actual).startsWith(0, 15, 70, 144); // 1_001_104 = 4 (length) + 1_000 (headers) + 1_000_100 (sep + content)
    }

    @Test
    void deserialize_shouldThrowTaskwarriorMessageDeserializationExceptionIfMessageIsSmallerThanFourBytes() {
        InputStream is = new ByteArrayInputStream(new byte[] { 1, 2 });

        assertThatThrownBy(() -> deserialize(is))
                .isInstanceOf(TaskwarriorMessageDeserializationException.class)
                .hasMessage("Encoded message length incomplete. Expected at least 4 bytes but only 2 are available.");
    }

    @Test
    void deserialize_shouldThrowTaskwarriorMessageDeserializationExceptionIfMessageLengthTellsMoreAsAvailable() {
        InputStream is = new ByteArrayInputStream(new byte[] { 0, 0, 0, 8, 24 });

        assertThatThrownBy(() -> deserialize(is))
                .isInstanceOf(TaskwarriorMessageDeserializationException.class)
                .hasMessage("Could not retrieve complete message. Missing 3 bytes.");
    }

    @Test
    void deserialize_shouldThrowTaskwarriorMessageDeserializationExceptionIfHeaderEntryIsBroken() {
        byte[] messageBytes = new byte[95];
        System.arraycopy(new byte[] { 0, 0, 0, 95 }, 0, messageBytes, 0, 4);
        byte[] message = join("\n", "invalid header value", "", "").getBytes(UTF_8);
        System.arraycopy(message, 0, messageBytes, 4, message.length);

        assertThatThrownBy(() -> deserialize(new ByteArrayInputStream(messageBytes)))
                .isInstanceOf(TaskwarriorMessageDeserializationException.class)
                .hasMessageMatching("Header line 'invalid header value' is not parsable, it must match '.*'.");
    }

    @Test
    void deserialize_shouldReturnCorrectMessageWithoutPayload() {
        byte[] messageBytes = new byte[95];
        System.arraycopy(new byte[] { 0, 0, 0, 95 }, 0, messageBytes, 0, 4);
        byte[] message = join(
                "\n",
                "org: org",
                "user: user",
                "key: " + uuid,
                "header1: val1",
                "header2: val2",
                "",
                "" // TODO maybe get rid, that this line has to be present and the before one as well in parser!!!
        ).getBytes(UTF_8);
        System.arraycopy(message, 0, messageBytes, 4, message.length);

        TaskwarriorMessage actual = deserialize(new ByteArrayInputStream(messageBytes));

        assertThat(actual.getHeaders()).containsOnly(
                entry("key", uuid.toString()),
                entry("org", "org"),
                entry("user", "user"),
                entry("header1", "val1"),
                entry("header2", "val2"));
        assertThat(actual.getPayload()).isNotPresent();
    }

    @Test
    void deserialize_shouldReturnCorrectMessageWithPayload() {
        byte[] messageBytes = new byte[124];
        System.arraycopy(new byte[] { 0, 0, 0, 124 }, 0, messageBytes, 0, 4);
        byte[] message = join(
                "\n",
                "org: org",
                "user: user",
                "key: " + uuid,
                "header1: val1",
                "header2: val2",
                "",
                "This is the expected payload."
        ).getBytes(UTF_8);
        System.arraycopy(message, 0, messageBytes, 4, message.length);

        TaskwarriorMessage actual = deserialize(new ByteArrayInputStream(messageBytes));

        assertThat(actual.getHeaders()).containsOnly(
                entry("key", uuid.toString()),
                entry("org", "org"),
                entry("user", "user"),
                entry("header1", "val1"),
                entry("header2", "val2"));
        assertThat(actual.getPayload()).hasValue("This is the expected payload.");
    }
}
