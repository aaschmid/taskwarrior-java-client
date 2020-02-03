package de.aaschmid.taskwarrior.message;

import java.util.HashMap;
import java.util.Map;

import de.aaschmid.taskwarrior.util.immutables.HiddenImplementationStyle;
import org.immutables.value.Value;

import static de.aaschmid.taskwarrior.message.ManifestHelper.getImplementationTitleAndVersionFromManifest;

/** Immutable request header of a taskwarrior message according to https://taskwarrior.org/docs/design/request.html#msgreq */
@Value.Immutable
@HiddenImplementationStyle
public interface TaskwarriorRequestHeader {

    String HEADER_KEY_CLIENT = "client";
    String HEADER_KEY_PROTOCOL = "protocol";
    String HEADER_KEY_TYPE = "type";

    String HEADER_AUTH_KEY_KEY = "key";
    String HEADER_AUTH_KEY_ORG = "org";
    String HEADER_AUTH_KEY_USER = "user";

    class Builder extends TaskwarriorRequestHeaderBuilder {}

    static Builder taskwarriorRequestHeaderBuilder() {
        return new Builder();
    }

    TaskwarriorAuthentication getAuthentication();

    MessageType getType();

    @Value.Default
    default String getClient() {
        return getImplementationTitleAndVersionFromManifest(this.getClass(), "taskwarrior-java-client");
    }

    @Value.Default
    default String getProtocol() {
        return "v1";
    }

    Map<String, String> getAdditionalHeaderEntries();

    default Map<String, String> toMap() {
        Map<String, String> result = new HashMap<>();

        result.put(HEADER_KEY_TYPE, getType().headerValue);
        result.put(HEADER_KEY_PROTOCOL, getProtocol());
        result.put(HEADER_KEY_CLIENT, getClient());

        result.put(HEADER_AUTH_KEY_KEY, getAuthentication().getAuthKey().toString());
        result.put(HEADER_AUTH_KEY_ORG, getAuthentication().getOrganization());
        result.put(HEADER_AUTH_KEY_USER, getAuthentication().getUser());

        result.putAll(getAdditionalHeaderEntries());
        return result;
    }
}
