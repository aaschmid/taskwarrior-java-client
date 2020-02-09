package de.aaschmid.taskwarrior.client;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

import de.aaschmid.taskwarrior.config.TaskwarriorConfiguration;
import de.aaschmid.taskwarrior.message.TaskwarriorMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static de.aaschmid.taskwarrior.client.TaskwarriorMessageFactory.deserialize;
import static de.aaschmid.taskwarrior.client.TaskwarriorMessageFactory.serialize;
import static java.util.Objects.requireNonNull;

public class TaskwarriorClient {

    private final TaskwarriorConfiguration config;
    private final SSLContext sslContext;

    public TaskwarriorClient(TaskwarriorConfiguration config) {
        this.config = requireNonNull(config, "'configuration' must not be null.");
        this.sslContext = SslContextFactory.createSslContext(config);
    }

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "generated try-with-resources code causes failure in Java 11, see https://github.com/spotbugs/spotbugs/issues/756")
    public TaskwarriorMessage sendAndReceive(TaskwarriorMessage message) {
        requireNonNull(message, "'message' must not be null.");

        System.out.println(config.getServerHost());
        try {
            System.out.println(InetSocketAddress.createUnresolved("localhost", config.getServerPort()));
            System.out.println(Arrays.toString(InetAddress.getAllByName("localhost")));
            System.out.println(InetAddress.getLocalHost());
            System.out.println(InetAddress.getLoopbackAddress());
            System.out.println(InetAddress.getByName("localhost"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Socket socket = sslContext.getSocketFactory().createSocket(InetAddress.getLoopbackAddress(), config.getServerPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (Socket socket = sslContext.getSocketFactory().createSocket(InetAddress.getLoopbackAddress(), config.getServerPort())) {
            return sendAndReceive(socket, message);
        } catch (IOException e) {
            throw new TaskwarriorClientException(
                    e,
                    "Could not create socket connection to '%s:%d'.",
                    config.getServerHost().getCanonicalHostName(),
                    config.getServerPort());
        }
    }

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "generated try-with-resources code causes failure in Java 11, see https://github.com/spotbugs/spotbugs/issues/756")
    private TaskwarriorMessage sendAndReceive(Socket socket, TaskwarriorMessage message) {
        try (OutputStream out = socket.getOutputStream(); InputStream in = socket.getInputStream()) {
            send(out, message);
            return receive(in);
        } catch (IOException e) {
            throw new TaskwarriorClientException("Could not open input and/or output stream of socket.", e);
        }
    }

    private void send(OutputStream out, TaskwarriorMessage message) {
        try {
            out.write(serialize(message));
            out.flush();
        } catch (IOException e) {
            throw new TaskwarriorClientException("Could not write and flush serialized message to output stream of socket.", e);
        }
    }

    private TaskwarriorMessage receive(InputStream in) {
        return deserialize(in);
    }
}
