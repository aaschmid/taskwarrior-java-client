package de.aaschmid.taskwarrior.message;

import de.aaschmid.taskwarrior.config.TaskwarriorAuthentication;
import de.aaschmid.taskwarrior.util.immutables.HiddenImplementationStyle;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;

@Value.Immutable @HiddenImplementationStyle
public interface TaskwarriorMessage {

    String HEADER_TYPE = "type";
    String HEADER_PROTOCOL = "protocol";
    String HEADER_CLIENT = "client";

    Map<String, String> getHeaders();

    Optional<String> getPayload();

    static TaskwarriorMessage taskwarriorMessage(Map<String, String> headers, String payload) {
        return ImmutableTaskwarriorMessage.builder().headers(headers).payload(payload).build();
    }

    static TaskwarriorMessage taskwarriorMessage(Map<String, String> headers) {
        return ImmutableTaskwarriorMessage.builder().headers(headers).build();
    }
}
