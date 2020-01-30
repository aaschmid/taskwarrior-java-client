package de.aaschmid.taskwarrior.message;

import java.util.Map;
import java.util.Optional;

import de.aaschmid.taskwarrior.util.immutables.HiddenImplementationStyle;
import org.immutables.value.Value;

@Value.Immutable
@HiddenImplementationStyle
public interface TaskwarriorMessage {

    String HEADER_TYPE = "type";
    String HEADER_PROTOCOL = "protocol";
    String HEADER_CLIENT = "client";

    static TaskwarriorMessage taskwarriorMessage(Map<String, String> headers, String payload) {
        return new TaskwarriorMessageBuilder().headers(headers).payload(payload).build();
    }

    static TaskwarriorMessage taskwarriorMessage(Map<String, String> headers) {
        return new TaskwarriorMessageBuilder().headers(headers).build();
    }

    Map<String, String> getHeaders();

    Optional<String> getPayload();
}
