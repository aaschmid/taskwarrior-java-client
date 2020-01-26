package de.aaschmid.taskwarrior;

import static de.aaschmid.taskwarrior.config.TaskwarriorConfiguration.taskwarriorPropertiesConfiguration;
import static org.junit.jupiter.api.Assertions.*;
import static de.aaschmid.taskwarrior.message.TaskwarriorMessage.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.aaschmid.taskwarrior.config.*;
import de.aaschmid.taskwarrior.internal.ManifestHelper;
import de.aaschmid.taskwarrior.message.TaskwarriorMessage;
import de.aaschmid.taskwarrior.test.IntegrationTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TaskwarriorClientIntegrationTest {

    private static final URL PROPERTIES_TASKWARRIOR = TaskwarriorClientIntegrationTest.class.getResource("/integTest.taskwarrior.properties");
    private static String IMPL_VERSION = ManifestHelper.getImplementationVersionFromManifest("local-dev");
    private static TaskwarriorConfiguration CONFIG = null;

    @BeforeAll
    static void initConfig() {
        if (PROPERTIES_TASKWARRIOR == null) {
            throw new IllegalStateException(
                    "No 'taskwarrior.properties' found on Classpath. "
                            + "Create it by copy and rename 'taskwarrior.properties.template'. "
                            + "Also fill in proper values.");
        }
        CONFIG = taskwarriorPropertiesConfiguration(PROPERTIES_TASKWARRIOR);
    }

    @IntegrationTest
    void configParsed() {
        assertNotNull(CONFIG);
        assertEquals("localhost", CONFIG.getServerHost().getCanonicalHostName());
        assertEquals(53589, CONFIG.getServerPort());
    }

    @IntegrationTest
    void statistics() throws IOException {

        TaskwarriorClient client = new TaskwarriorClient(CONFIG);

        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TYPE, "statistics");
        headers.put(HEADER_PROTOCOL, "v1");
        headers.put(HEADER_CLIENT, "taskwarrior-java-client " + IMPL_VERSION);

        TaskwarriorMessage response = client.sendAndReceive(taskwarriorMessage(headers));
        assertEquals("200", response.getHeaders().get("code"));
    }


    @IntegrationTest
    void sync() throws IOException {

        TaskwarriorClient client = new TaskwarriorClient(CONFIG);

        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TYPE, "sync");
        headers.put(HEADER_PROTOCOL, "v1");
        headers.put(HEADER_CLIENT, "taskwarrior-java-client " + IMPL_VERSION);

        TaskwarriorMessage response = client.sendAndReceive(taskwarriorMessage(headers));
        assertEquals("200", response.getHeaders().get("code"));
    }
}