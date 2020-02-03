package de.aaschmid.taskwarrior.client;

import java.io.File;

public interface TaskwarriorSslKeys {

    File getCaCertFile();
    File getPrivateKeyCertFile();
    File getPrivateKeyFile();
}
