package de.aaschmid.taskwarrior.client;

import de.aaschmid.taskwarrior.TaskwarriorException;

/** Exception occurs if a problem within {@link TaskwarriorClient} occurs. */
public class TaskwarriorClientException extends TaskwarriorException {

    private static final long serialVersionUID = -1523127582255643797L;

    public TaskwarriorClientException(Throwable cause, String format, Object... args) {
        super(cause, format, args);
    }

    public TaskwarriorClientException(String format, Object... args) {
        super(format, args);
    }
}
