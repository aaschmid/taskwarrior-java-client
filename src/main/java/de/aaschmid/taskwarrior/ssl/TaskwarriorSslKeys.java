package de.aaschmid.taskwarrior.ssl;

import java.io.File;

public interface TaskwarriorSslKeys {

    File getCaCertFile();
    File getPrivateKeyCertFile();
    File getPrivateKeyFile();
}
