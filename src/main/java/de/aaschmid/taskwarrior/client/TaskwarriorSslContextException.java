package de.aaschmid.taskwarrior.client;

import javax.net.ssl.SSLContext;

import de.aaschmid.taskwarrior.TaskwarriorException;

/** Exception occurs if a problem with the {@link SSLContext} configuration / creation occurs. */
public class TaskwarriorSslContextException extends TaskwarriorException {

    private static final long serialVersionUID = -976739870344497348L;

    public TaskwarriorSslContextException(Throwable cause, String format, Object... args) {
        super(cause, format, args);
    }
}
