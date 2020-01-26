package de.aaschmid.taskwarrior;

import de.aaschmid.taskwarrior.config.TaskwarriorConfiguration;
import de.aaschmid.taskwarrior.message.TaskwarriorMessage;
import de.aaschmid.taskwarrior.ssl.KeyStoreBuilder;
import de.aaschmid.taskwarrior.ssl.SslContextFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyStore;

import static de.aaschmid.taskwarrior.message.TaskwarriorMessageFactory.deserialize;
import static de.aaschmid.taskwarrior.message.TaskwarriorMessageFactory.serialize;
import static java.util.Objects.requireNonNull;

public class TaskwarriorClient {

    private final TaskwarriorConfiguration config;
    private final SSLContext sslContext;

    public TaskwarriorClient(TaskwarriorConfiguration config) {
        this.config = requireNonNull(config, "'configuration' must not be null.");

        // @formatter:off
        KeyStore keyStore = new KeyStoreBuilder()
                .withCaCertFile(config.getCaCertFile())
                .withPrivateKeyCertFile(config.getPrivateKeyCertFile())
                .withPrivateKeyFile(config.getPrivateKeyFile())
                .build();
        // @formatter:on
        this.sslContext = SslContextFactory.getInstance(keyStore, KeyStoreBuilder.KEYSTORE_PASSWORD);
    }

    public TaskwarriorMessage sendAndReceive(TaskwarriorMessage message) throws IOException {
        requireNonNull(message, "'message' must not be null.");

        try (Socket socket = sslContext.getSocketFactory().createSocket(config.getServerHost(), config.getServerPort());
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {
            send(message, out);
            return receive(in);
        }
    }

    private void send(TaskwarriorMessage message, OutputStream out) throws IOException {
        out.write(serialize(config.getAuthentication(), message));
        out.flush();
    }

    private TaskwarriorMessage receive(InputStream in) throws IOException {
        return deserialize(in);
    }
}
