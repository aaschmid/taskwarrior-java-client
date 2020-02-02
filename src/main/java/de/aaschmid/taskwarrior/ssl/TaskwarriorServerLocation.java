package de.aaschmid.taskwarrior.ssl;

import java.net.InetAddress;

public interface TaskwarriorServerLocation {

    InetAddress getServerHost();
    int getServerPort();
}