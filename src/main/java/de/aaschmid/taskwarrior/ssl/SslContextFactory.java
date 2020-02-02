package de.aaschmid.taskwarrior.ssl;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class SslContextFactory {

    private static final String DEFAULT_PROTOCOL = "TLS";

    public static SSLContext createSslContext(TaskwarriorSslKeys sslKeys) {
        requireNonNull(sslKeys, "'sslKeys' must not be null.");

        String keystorePassword = UUID.randomUUID().toString();

        KeyStore keyStore = new KeyStoreBuilder()
                .withPasswordProtection(keystorePassword)
                .withCaCertFile(sslKeys.getCaCertFile())
                .withPrivateKeyCertFile(sslKeys.getPrivateKeyCertFile())
                .withPrivateKeyFile(sslKeys.getPrivateKeyFile())
                .build();

        return createSslContext(DEFAULT_PROTOCOL, keyStore, keystorePassword);
    }

    /** @param protocol see {@link SSLContext#getInstance(String)} for valid protocols */
    static SSLContext createSslContext(String protocol, KeyStore keyStore, String keyStorePassword) {
        requireNonNull(protocol, "'protocol' must not be null.");
        requireNonNull(keyStore, "'keyStore' must not be null.");
        requireNonNull(keyStorePassword, "'keyStorePassword' must not be null.");

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance(protocol);
        } catch (NoSuchAlgorithmException e) {
            throw new TaskwarriorSslContextException(e, "Cannot create SSL context for protocol '%s'.", protocol);
        }
        try {
            sslContext.init(loadKeyMaterial(keyStore, keyStorePassword), loadTrustMaterial(keyStore), null);
        } catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new TaskwarriorSslContextException(e, "Could not init ssl context: %s", e.getMessage());
        }
        return sslContext;
    }

    static SSLContext createSslContext(KeyStore keyStore, String keyStorePassword) {
        return createSslContext(DEFAULT_PROTOCOL, keyStore, keyStorePassword);
    }

    private static KeyManager[] loadKeyMaterial(KeyStore keystore, String keyStorePassword)
            throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        KeyManagerFactory result = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        result.init(keystore, keyStorePassword.toCharArray());
        return result.getKeyManagers();
    }

    private static TrustManager[] loadTrustMaterial(KeyStore truststore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory result = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        result.init(truststore);
        return result.getTrustManagers();
    }
}
