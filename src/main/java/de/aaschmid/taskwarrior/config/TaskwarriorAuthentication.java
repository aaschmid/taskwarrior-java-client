package de.aaschmid.taskwarrior.config;

import java.util.UUID;

import de.aaschmid.taskwarrior.util.immutables.HiddenImplementationStyle;
import org.immutables.value.Value.Immutable;

/**
 * Immutable class containing the relevant taskwarrior authentication information.
 */
@Immutable
@HiddenImplementationStyle
public interface TaskwarriorAuthentication {

    static TaskwarriorAuthentication taskwarriorAuthentication(String org, UUID key, String user) {
        return new TaskwarriorAuthenticationBuilder().organisation(org).key(key).user(user).build();
    }

    String getOrganisation();

    UUID getKey();

    String getUser();
}