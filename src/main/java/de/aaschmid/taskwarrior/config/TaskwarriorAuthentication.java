package de.aaschmid.taskwarrior.config;

import de.aaschmid.taskwarrior.util.immutables.HiddenImplementationStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

/**
 * Immutable class containing the relevant taskwarrior authentication information.
 */
@Immutable @HiddenImplementationStyle
public interface TaskwarriorAuthentication {

    static TaskwarriorAuthentication taskwarriorAuthentication(String org, UUID key, String user) {
        return ImmutableTaskwarriorAuthentication.builder().organisation(org).key(key).user(user).build();
    }

    String getOrganisation();

    UUID getKey();

    String getUser();
}