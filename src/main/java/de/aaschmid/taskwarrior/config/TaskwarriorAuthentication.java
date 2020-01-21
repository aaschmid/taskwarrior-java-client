package de.aaschmid.taskwarrior.config;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable class containing the relevant taskwarrior authentication information.
 */
public class TaskwarriorAuthentication {
    private final String organisation;
    private final UUID key;
    private final String user;

    public TaskwarriorAuthentication(String organisation, UUID key, String user) {
        this.organisation = requireNonNull(organisation, "'organisation' must not be null.");
        this.key = requireNonNull(key, "'key' must not be null.");
        this.user = requireNonNull(user, "'user' must not be null.");
    }

    public String getOrganisation() {
        return organisation;
    }

    public UUID getKey() {
        return key;
    }

    public String getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskwarriorAuthentication)) {
            return false;
        }
        TaskwarriorAuthentication that = (TaskwarriorAuthentication) o;
        return Objects.equals(organisation, that.organisation) &&
                Objects.equals(key, that.key) &&
                Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organisation, key, user);
    }

    @Override
    public String toString() {
        return "TaskwarriorAuthentication{" +
                "organisation='" + organisation + '\'' +
                ", key=" + key +
                ", user='" + user + '\'' +
                '}';
    }
}