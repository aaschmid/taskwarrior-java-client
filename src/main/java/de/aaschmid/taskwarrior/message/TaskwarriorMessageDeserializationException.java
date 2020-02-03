package de.aaschmid.taskwarrior.message;

import de.aaschmid.taskwarrior.TaskwarriorException;

/** Exception occurs if a problem within the deserialization of a {@link TaskwarriorMessage} occurs. */
public class TaskwarriorMessageDeserializationException extends TaskwarriorException {

    private static final long serialVersionUID = -4817349426539597530L;

    public TaskwarriorMessageDeserializationException(String format, Object... args) {
        super(format, args);
    }
}
