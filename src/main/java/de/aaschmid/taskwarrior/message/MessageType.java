package de.aaschmid.taskwarrior.message;

public enum MessageType {
    STATISTICS("statistics"),
    SYNC("sync");

    public final String headerValue;

    MessageType(String headerValue) {
        this.headerValue = headerValue;
    }
}
