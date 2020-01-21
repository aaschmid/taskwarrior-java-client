package de.aaschmid.taskwarrior.config;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class TaskwarriorAuthenticationTest {

    @Test
    void testEqualsAndHashCode() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        new EqualsTester()
                .addEqualityGroup(
                        new TaskwarriorAuthentication("Org", uuid1, "User"),
                        new TaskwarriorAuthentication("Org", uuid1, "User")
                )
                .addEqualityGroup(
                        new TaskwarriorAuthentication("OtherOrg", uuid1, "User"),
                        new TaskwarriorAuthentication("OtherOrg", uuid1, "User")
                )
                .addEqualityGroup(
                        new TaskwarriorAuthentication("Org", uuid2, "User"),
                        new TaskwarriorAuthentication("Org", uuid2, "User")
                )
                .addEqualityGroup(
                        new TaskwarriorAuthentication("Org", uuid2, "OtherUser"),
                        new TaskwarriorAuthentication("Org", uuid2, "OtherUser")
                )
                .testEquals();
    }
}