package de.aaschmid.taskwarrior.message;

import de.aaschmid.taskwarrior.TaskwarriorException;

/**
 * Exception occurs if a problem within the deserialization of a {@link TaskwarriorMessage} occurs.
 */
public class TaskwarriorMessageDeserializationException extends TaskwarriorException {

    private static final long serialVersionUID = 1L;

    public TaskwarriorMessageDeserializationException(Throwable cause, String message) {
        super(message, cause);
    }

    public TaskwarriorMessageDeserializationException(Throwable cause, String format, Object... args) {
        super(cause, format, args);
    }

    public TaskwarriorMessageDeserializationException(String message) {
        super(message);
    }

    public TaskwarriorMessageDeserializationException(String format, Object... args) {
        super(format, args);
    }
}
