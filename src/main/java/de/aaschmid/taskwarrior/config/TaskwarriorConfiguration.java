package de.aaschmid.taskwarrior.config;

import java.net.URL;

import de.aaschmid.taskwarrior.client.TaskwarriorServerLocation;
import de.aaschmid.taskwarrior.client.TaskwarriorSslKeys;
import de.aaschmid.taskwarrior.message.TaskwarriorAuthentication;

public interface TaskwarriorConfiguration extends TaskwarriorServerLocation, TaskwarriorSslKeys, TaskwarriorAuthentication {

    static TaskwarriorPropertiesConfiguration taskwarriorPropertiesConfiguration(URL propertiesUrl) {
        return new TaskwarriorPropertiesConfiguration(propertiesUrl);
    }
}