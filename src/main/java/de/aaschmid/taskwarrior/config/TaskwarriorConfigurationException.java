package de.aaschmid.taskwarrior.config;

import de.aaschmid.taskwarrior.TaskwarriorException;

/** Exception occurs if any {@link TaskwarriorConfiguration} problem occurs. */
public class TaskwarriorConfigurationException extends TaskwarriorException {

    private static final long serialVersionUID = -7577644291974327797L;

    public TaskwarriorConfigurationException(Throwable cause, String format, Object... args) {
        super(cause, format, args);
    }

    public TaskwarriorConfigurationException(String format, Object... args) {
        super(format, args);
    }
}
