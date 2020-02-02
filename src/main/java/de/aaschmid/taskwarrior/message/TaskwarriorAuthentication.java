package de.aaschmid.taskwarrior.message;

import java.util.UUID;

import de.aaschmid.taskwarrior.util.immutables.HiddenImplementationStyle;
import org.immutables.value.Value.Immutable;

@Immutable
@HiddenImplementationStyle
public interface TaskwarriorAuthentication {

    static TaskwarriorAuthentication taskwarriorAuthentication(UUID key, String org, String user) {
        return new TaskwarriorAuthenticationBuilder().authKey(key).organization(org).user(user).build();
    }

    UUID getAuthKey();
    String getOrganization();
    String getUser();
}
