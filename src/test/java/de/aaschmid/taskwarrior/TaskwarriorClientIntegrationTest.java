package de.aaschmid.taskwarrior;

import java.io.IOException;
import java.net.URL;

import de.aaschmid.taskwarrior.client.TaskwarriorClient;
import de.aaschmid.taskwarrior.config.TaskwarriorConfiguration;
import de.aaschmid.taskwarrior.message.MessageType;
import de.aaschmid.taskwarrior.message.TaskwarriorMessage;
import de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader;
import de.aaschmid.taskwarrior.test.IntegrationTest;

import static de.aaschmid.taskwarrior.config.TaskwarriorConfiguration.taskwarriorPropertiesConfiguration;
import static de.aaschmid.taskwarrior.message.TaskwarriorMessage.taskwarriorMessage;
import static de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader.taskwarriorRequestHeaderBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class TaskwarriorClientIntegrationTest {

    private static final URL PROPERTIES_TASKWARRIOR =
            TaskwarriorClientIntegrationTest.class.getResource("/integTest.taskwarrior.properties");
    private static final TaskwarriorConfiguration CONFIG = taskwarriorPropertiesConfiguration(PROPERTIES_TASKWARRIOR);

    private static final String SYNC_KEY = "f92d5c8d-4cf9-4cf5-b72f-1f4a70cf9b20";

    private final TaskwarriorClient client = new TaskwarriorClient(CONFIG);

    @IntegrationTest
    void statistics() throws IOException {
        TaskwarriorRequestHeader header = taskwarriorRequestHeaderBuilder()
                .authentication(CONFIG)
                .type(MessageType.STATISTICS)
                .build();
        TaskwarriorMessage message = taskwarriorMessage(header.toMap());

        TaskwarriorMessage response = client.sendAndReceive(message);

        assertThat(response.getHeaders())
                .contains(entry("code", "200"))
                .contains(entry("errors", "0"))
                .contains(entry("organizations", "1"))
                .contains(entry("status", "Ok"))
                .contains(entry("users", "1"));
        assertThat(response.getPayload()).isNotPresent();
    }

    @IntegrationTest
    void syncWithoutSyncKey() throws IOException { // FIXME maybe get rid of ugly IOException
        TaskwarriorRequestHeader header = taskwarriorRequestHeaderBuilder()
                .authentication(CONFIG)
                .type(MessageType.SYNC)
                .build();
        TaskwarriorMessage message = taskwarriorMessage(header.toMap());

        TaskwarriorMessage response = client.sendAndReceive(message);

        assertThat(response.getHeaders())
                .contains(entry("code", "200"))
                .contains(entry("status", "Ok"));
        assertThat(response.getPayload()).hasValueSatisfying(payload -> assertThat(payload).startsWith(
                "{\"description\":\"some task\",\"entry\":\"20190831T170318Z\",\"modified\":\"20190831T170318Z\",\"status\":\"pending\",\"uuid\":\"1e8cd315-c78b-46f6-bdbd-64caf83c275a\"}")
                .endsWith(SYNC_KEY));
    }

    @IntegrationTest
    void syncWithSyncKey() throws IOException {
        TaskwarriorRequestHeader header = taskwarriorRequestHeaderBuilder()
                .authentication(CONFIG)
                .type(MessageType.SYNC)
                .build();
        TaskwarriorMessage message = taskwarriorMessage(header.toMap(), SYNC_KEY);

        TaskwarriorMessage response = client.sendAndReceive(message);

        assertThat(response.getHeaders())
                .contains(entry("code", "201"))
                .contains(entry("status", "No change"));
        assertThat(response.getPayload()).hasValue(SYNC_KEY);
    }
}
