package de.aaschmid.taskwarrior;

import java.util.stream.Stream;

import de.aaschmid.taskwarrior.client.TaskwarriorClient;
import de.aaschmid.taskwarrior.config.TaskwarriorConfiguration;
import de.aaschmid.taskwarrior.message.TaskwarriorMessage;
import de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader;
import de.aaschmid.taskwarrior.test.IntegrationTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static de.aaschmid.taskwarrior.message.TaskwarriorMessage.taskwarriorMessage;
import static de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader.taskwarriorRequestHeaderBuilder;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

@IntegrationTest
class TaskwarriorClientIntegrationTest {

    private static final String SYNC_KEY = "f92d5c8d-4cf9-4cf5-b72f-1f4a70cf9b20";

    static Stream<Arguments> configs() {
        return Stream.of("pkcs1", "pkcs8", "pkcs8-der")
                .map(keyType -> format("/taskwarrior.%s.properties", keyType))
                .map(TaskwarriorClientIntegrationTest.class::getResource)
                .map(TaskwarriorConfiguration::taskwarriorPropertiesConfiguration)
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("configs")
    void statistics(TaskwarriorConfiguration config) {
        TaskwarriorRequestHeader header = taskwarriorRequestHeaderBuilder()
                .authentication(config)
                .type(TaskwarriorRequestHeader.MessageType.STATISTICS)
                .build();
        TaskwarriorMessage message = taskwarriorMessage(header.toMap());

        TaskwarriorMessage response = clientFor(config).sendAndReceive(message);

        assertThat(response.getHeaders())
                .contains(entry("code", "200"))
                .contains(entry("errors", "0"))
                .contains(entry("organizations", "1"))
                .contains(entry("status", "Ok"))
                .contains(entry("users", "1"));
        assertThat(response.getPayload()).isNotPresent();
    }

    @ParameterizedTest
    @MethodSource("configs")
    void syncWithoutSyncKey(TaskwarriorConfiguration config) {
        TaskwarriorRequestHeader header = taskwarriorRequestHeaderBuilder()
                .authentication(config)
                .type(TaskwarriorRequestHeader.MessageType.SYNC)
                .build();
        TaskwarriorMessage message = taskwarriorMessage(header.toMap());

        TaskwarriorMessage response = clientFor(config).sendAndReceive(message);

        assertThat(response.getHeaders())
                .contains(entry("code", "200"))
                .contains(entry("status", "Ok"));
        assertThat(response.getPayload()).hasValueSatisfying(payload -> assertThat(payload).startsWith(
                "{\"description\":\"some task\",\"entry\":\"20190831T170318Z\",\"modified\":\"20190831T170318Z\",\"status\":\"pending\",\"uuid\":\"1e8cd315-c78b-46f6-bdbd-64caf83c275a\"}")
                .endsWith(SYNC_KEY));
    }

    @ParameterizedTest
    @MethodSource("configs")
    void syncWithSyncKey(TaskwarriorConfiguration config) {
        TaskwarriorRequestHeader header = taskwarriorRequestHeaderBuilder()
                .authentication(config)
                .type(TaskwarriorRequestHeader.MessageType.SYNC)
                .build();
        TaskwarriorMessage message = taskwarriorMessage(header.toMap(), SYNC_KEY);

        TaskwarriorMessage response = clientFor(config).sendAndReceive(message);

        assertThat(response.getHeaders())
                .contains(entry("code", "201"))
                .contains(entry("status", "No change"));
        assertThat(response.getPayload()).hasValue(SYNC_KEY);
    }

    private TaskwarriorClient clientFor(TaskwarriorConfiguration config) {
        return new TaskwarriorClient(config);
    }
}
