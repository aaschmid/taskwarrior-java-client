package de.aaschmid.taskwarrior;

import static org.junit.jupiter.api.Assertions.*;
import static de.aaschmid.taskwarrior.message.TaskwarriorMessage.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.aaschmid.taskwarrior.config.*;
import de.aaschmid.taskwarrior.internal.ManifestHelper;
import de.aaschmid.taskwarrior.message.TaskwarriorMessage;

class TaskwarriorClientTest {

    private static final URL PROPERTIES_TASKWARRIOR = TaskwarriorClientTest.class.getResource("/taskwarrior.properties");

    public static void main(String[] args) throws Exception {
        if (PROPERTIES_TASKWARRIOR == null) {
            throw new IllegalStateException(
                    "No 'taskwarrior.properties' found on Classpath. Create it by copy and rename 'taskwarrior.properties.template'. Also fill in proper values.");
        }
        TaskwarriorConfiguration config = new TaskwarriorPropertiesConfiguration(PROPERTIES_TASKWARRIOR);

        TaskwarriorClient client = new TaskwarriorClient(config);

        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TYPE, "statistics");
        headers.put(HEADER_PROTOCOL, "v1");
        headers.put(HEADER_CLIENT, "taskwarrior-java-client " + ManifestHelper.getImplementationVersionFromManifest("local-dev"));

        TaskwarriorMessage response = client.sendAndReceive(new TaskwarriorMessage(headers));
        System.out.println(response);
    }
}
