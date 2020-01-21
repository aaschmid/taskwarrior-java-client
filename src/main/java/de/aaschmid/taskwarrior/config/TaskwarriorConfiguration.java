package de.aaschmid.taskwarrior.config;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;

public interface TaskwarriorConfiguration {

    static TaskwarriorPropertiesConfiguration taskwarriorPropertiesConfiguration(URL propertiesUrl) {
        return new TaskwarriorPropertiesConfiguration(propertiesUrl);
    }

    File getCaCertFile();

    File getPrivateKeyCertFile();

    File getPrivateKeyFile();

    InetAddress getServerHost();

    int getServerPort();

    TaskwarriorAuthentication getAuthentication();
}