package de.aaschmid.taskwarrior.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.aaschmid.taskwarrior.config.TaskwarriorAuthentication;
import org.junit.jupiter.api.Test;

import static de.aaschmid.taskwarrior.config.TaskwarriorAuthentication.taskwarriorAuthentication;
import static de.aaschmid.taskwarrior.message.TaskwarriorMessageFactory.deserialize;
import static de.aaschmid.taskwarrior.message.TaskwarriorMessageFactory.serialize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

class TaskwarriorMessageFactoryTest {

    private final UUID uuid = UUID.randomUUID();

    @Test
    void serialize_shouldReturnCorrectBytesArray() {
        TaskwarriorAuthentication auth = taskwarriorAuthentication("org", uuid, "user");
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("header1", "value1");
        headers.put("header2", "value2");
        String payload = "This is the expected payload.";

        byte[] actual = serialize(auth, TaskwarriorMessage.taskwarriorMessage(headers, payload));

        assertThat(actual)
                .startsWith(0, 0, 0, actual.length)
                .endsWith(String.format(
                        "org: org\nuser: user\nkey: %s\nheader1: value1\nheader2: value2\n\nThis is the expected payload.",
                        uuid).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void serialize_shouldReturnCorrectMessageLengthInResultingBytesArray() {
        TaskwarriorAuthentication auth = taskwarriorAuthentication("org", uuid, "user");
        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 0; i < 100; i++) {
            headers.put(String.format("h%03d", i), "val");
        }
        List<String> lines = new ArrayList<>();
        for (int i = 0; i <= 10_000; i++) {
            lines.add(String.format("%099d", i));
        }

        byte[] actual = serialize(auth, TaskwarriorMessage.taskwarriorMessage(headers, String.join("\n", lines)));

        assertThat(actual).startsWith(0, 15, 70, 206); // 1_001_166 = 4 (length) + 1_000 (headers) + 62 (auth) + 1_000_100 (sep + content)
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
    void deserialize_shouldThrowTaskwarriorMessageDeserializationExceptionIfHeaderEntryIsBroken() throws IOException {
        byte[] messageBytes = new byte[95];
        System.arraycopy(new byte[] { 0, 0, 0, 95 }, 0, messageBytes, 0, 4);
        byte[] message = String.join("\n", "invalid header value", "", "").getBytes(StandardCharsets.UTF_8);
        System.arraycopy(message, 0, messageBytes, 4, message.length);

        assertThatThrownBy(() -> deserialize(new ByteArrayInputStream(messageBytes)))
                .isInstanceOf(TaskwarriorMessageDeserializationException.class)
                .hasMessageMatching("Header line 'invalid header value' is not parsable, it must match '.*'.");

    }

    @Test
    void deserialize_shouldReturnCorrectMessageWithoutPayload() throws IOException {
        byte[] messageBytes = new byte[95];
        System.arraycopy(new byte[] { 0, 0, 0, 95 }, 0, messageBytes, 0, 4);
        byte[] message = String.join(
                "\n",
                "org: org",
                "user: user",
                "key: " + uuid,
                "header1: val1",
                "header2: val2",
                "",
                "" // TODO maybe get rid, that this line has to be present and the before one as well in parser!!!
        ).getBytes(StandardCharsets.UTF_8);
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
    void deserialize_shouldReturnCorrectMessageWithPayload() throws IOException {
        byte[] messageBytes = new byte[124];
        System.arraycopy(new byte[] { 0, 0, 0, 124 }, 0, messageBytes, 0, 4);
        byte[] message = String.join(
                "\n",
                "org: org",
                "user: user",
                "key: " + uuid,
                "header1: val1",
                "header2: val2",
                "",
                "This is the expected payload."
        ).getBytes(StandardCharsets.UTF_8);
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
