package de.aaschmid.taskwarrior;

import static java.lang.String.format;

/**
 * Exception occurs if any taskwarrior problem occurs.
 */
public class TaskwarriorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TaskwarriorException(Throwable cause, String message) {
        super(message, cause);
    }

    public TaskwarriorException(Throwable cause, String format, Object... args) {
        this(cause, format(format, args));
    }

    public TaskwarriorException(String message) {
        super(message);
    }

    public TaskwarriorException(String format, Object... args) {
        this(format(format, args));
    }
}
