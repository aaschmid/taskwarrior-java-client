package de.aaschmid.taskwarrior.message;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.aaschmid.taskwarrior.task.TaskwarriorTask;

public class TaskwarriorMessageFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public static TaskwarriorMessage messageFor(TaskwarriorRequest request) {
        requireNonNull(request, "'request' must not be null.");
        return new TaskwarriorMessage(createHeadersFor(request), createPayloadFor(request));
    }

    public static TaskwarriorResponse responseFor(TaskwarriorMessage message) {
        requireNonNull(message, "'message' must not be null.");

        String client = null;
        int code = -1;
        String status = null;
        for (Entry<String, String> header : message.getHeaders().entrySet()) {
            switch (header.getKey()) {
            case "client":
                client = header.getValue();
                break;
            case "code":
                code = Integer.valueOf(header.getValue());
                break;
            case "status":
                status = header.getValue();
                break;
            default:
                throw new IllegalArgumentException("Unknown header field '" + header.getKey() + "'. Value is '" + header.getValue() + "'.");
            }
        }

        UUID syncKey = null;
        List<TaskwarriorTask> tasks = new ArrayList<>();
        for (String payloadLine : message.getPayload().map(s -> s.split("\n")).orElse(new String[0])) {
            if (payloadLine.startsWith("{")) { // JSON
                tasks.add(createTaskFor(payloadLine));
            } else if (!payloadLine.isEmpty()) { // Sync key
                syncKey = UUID.fromString(payloadLine);
            }
        }

        return new TaskwarriorResponse(client, code, status, syncKey, tasks);
    }

    private static Map<String, String> createHeadersFor(TaskwarriorRequest request) {
        Map<String, String> result = new HashMap<>();
        result.put("type", request.getType().value);
        result.put("protocol", request.getProtocol().value);
        result.put("client", request.getClient());
        return result;
    }

    private static String createPayloadFor(TaskwarriorRequest request) {
        StringBuilder result = new StringBuilder();
        if (request.getSyncKey() != null) {
            result.append(request.getSyncKey().toString()).append("\n");
        }
        result.append(request.getTasks().stream().map(t -> createJsonFor(t)).collect(Collectors.joining("\n")));
        return result.toString();
    }

    private static String createJsonFor(TaskwarriorTask task) {
        try {
            return OBJECT_MAPPER.writeValueAsString(task);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not create string representation of task '" + task.toString() + "'.", e);
        }
    }

    private static TaskwarriorTask createTaskFor(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, TaskwarriorTask.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not convert JSON '" + json + "' to " + TaskwarriorTask.class + ".", e);
        }
    }
}
