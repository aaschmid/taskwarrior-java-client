package de.aaschmid.taskwarrior.client;

import java.net.InetAddress;

public interface TaskwarriorServerLocation {

    InetAddress getServerHost();
    int getServerPort();
}