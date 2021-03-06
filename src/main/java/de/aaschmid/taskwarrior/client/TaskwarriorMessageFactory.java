package de.aaschmid.taskwarrior.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.aaschmid.taskwarrior.message.TaskwarriorMessage;
import de.aaschmid.taskwarrior.message.TaskwarriorMessageDeserializationException;

import static de.aaschmid.taskwarrior.message.TaskwarriorMessage.taskwarriorMessage;

class TaskwarriorMessageFactory {

    private static final Charset CHARSET_TRANSFER_MESSAGE = StandardCharsets.UTF_8;

    private static final String SEPARATOR_HEADER_NAME_VALUE = ": ";
    private static final Pattern PATTERN_HEADER_LINE = Pattern.compile("^(.+?)" + SEPARATOR_HEADER_NAME_VALUE + "(.+)$");

    static byte[] serialize(TaskwarriorMessage message) {
        String messageData = Stream.concat(Stream.of(message.getHeaders())
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .map(e -> e.getKey() + SEPARATOR_HEADER_NAME_VALUE + e.getValue()), Stream.of("", message.getPayload().orElse("")))
                .collect(Collectors.joining("\n"));
        byte[] bytes = messageData.getBytes(CHARSET_TRANSFER_MESSAGE);
        return addFourByteBigEndianBinaryByteCountMessageLengthPrefix(bytes);
    }

    static TaskwarriorMessage deserialize(InputStream in) {
        int messageLength = receiveRemainingMessageLengthFromFourByteBigEndianBinaryByteCountPrefix(in);
        byte[] data = readMessageAsByteArray(in, messageLength);
        return parseResponse(new String(data, CHARSET_TRANSFER_MESSAGE));
    }

    private static int receiveRemainingMessageLengthFromFourByteBigEndianBinaryByteCountPrefix(InputStream in) {
        byte[] sizeBytes = new byte[4];
        try {
            int length = in.read(sizeBytes);
            if (length != 4) {
                throw new TaskwarriorMessageDeserializationException(
                        "Encoded message length incomplete. Expected at least 4 bytes but only %d are available.",
                        length);
            }
        } catch (IOException e) {
            throw new TaskwarriorMessageDeserializationException("Could not read 4-byte, big-endian, binary byte count.", e);
        }
        return ((sizeBytes[0] << 24) | (sizeBytes[1] << 16) | (sizeBytes[2] << 8) | sizeBytes[3]) - 4;
    }

    private static byte[] readMessageAsByteArray(InputStream in, int messageLength) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int readCount;
        int remaining = messageLength;
        byte[] buffer = new byte[1024];
        try {
            while ((readCount = in.read(buffer)) != -1) {
                out.write(buffer, 0, readCount);
                remaining -= readCount;
            }

            if (remaining > 0) {
                throw new TaskwarriorMessageDeserializationException("Could not retrieve complete message. Missing %d bytes.", remaining);
            }

            out.flush();
        } catch (IOException e) {
            throw new TaskwarriorMessageDeserializationException("Could not bytes of the message according to calculated length.", e);
        }
        return out.toByteArray();
    }

    private static TaskwarriorMessage parseResponse(String message) {
        int index = message.indexOf("\n\n");

        String header = message.substring(0, index);
        String payload = message.substring(index + 2);

        Map<String, String> headers = parseHeaders(header);
        if (payload.isEmpty() || "\n".equals(payload)) {
            return taskwarriorMessage(headers);
        }
        return taskwarriorMessage(headers, payload.trim());
    }

    private static byte[] addFourByteBigEndianBinaryByteCountMessageLengthPrefix(byte[] bytes) {
        byte[] result = new byte[bytes.length + 4];
        System.arraycopy(bytes, 0, result, 4, bytes.length);

        int l = result.length;
        result[0] = (byte) (l >> 24);
        result[1] = (byte) (l >> 16);
        result[2] = (byte) (l >> 8);
        result[3] = (byte) l;
        return result;
    }

    private static Map<String, String> parseHeaders(String header) {
        Map<String, String> headers = new HashMap<>();
        for (String headerLine : header.split("\n")) {
            Matcher matcher = PATTERN_HEADER_LINE.matcher(headerLine);
            if (!matcher.matches()) {
                throw new TaskwarriorMessageDeserializationException(
                        "Header line '%s' is not parsable, it must match '%s'.",
                        headerLine,
                        PATTERN_HEADER_LINE.pattern());
            }
            String name = matcher.group(1);
            String value = matcher.group(2);
            headers.put(name, value);
        }
        return headers;
    }
}
