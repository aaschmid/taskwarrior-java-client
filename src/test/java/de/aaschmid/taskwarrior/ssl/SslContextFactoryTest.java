package de.aaschmid.taskwarrior.ssl;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SslContextFactoryTest {

    @Test
    void createSslContext_shouldThrowNullPointerExceptionIfSslKeysIsNull() {
        assertThatThrownBy(() -> SslContextFactory.createSslContext(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'sslKeys' must not be null.");
    }

    @Test
    void createSslContext_shouldThrowNullPointerExceptionIfProtocolIsNull() {
        assertThatThrownBy(() -> SslContextFactory.createSslContext(null, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'protocol' must not be null.");
    }

    @Test
    void createSslContext_shouldThrowNullPointerExceptionIfKeyStoreIsNull() {
        assertThatThrownBy(() -> SslContextFactory.createSslContext("SSL", null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'keyStore' must not be null.");
    }

    @Test
    void createSslContext_shouldThrowNullPointerExceptionIfKeyStorePasswordIsNull() throws KeyStoreException {
        KeyStore keyStore = createDefaultKeyStore("test1");
        assertThatThrownBy(() -> SslContextFactory.createSslContext("SSL", keyStore, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'keyStorePassword' must not be null.");
    }

    @Test
    void createSslContext_shouldThrowTaskwarriorSslContextExceptionIfProtocolIsInvalid() throws KeyStoreException {
        KeyStore keyStore = createDefaultKeyStore("test2");
        assertThatThrownBy(() -> SslContextFactory.createSslContext("invalid", keyStore, "test"))
                .isInstanceOf(TaskwarriorSslContextException.class)
                .hasMessage("Cannot create SSL context for protocol 'invalid'.")
                .hasCauseInstanceOf(NoSuchAlgorithmException.class);
    }

    @Test
    void createSslContext_shouldReturnSslContext() throws KeyStoreException {
        KeyStore keyStore = createDefaultKeyStore("test3");
        SSLContext sslContext = SslContextFactory.createSslContext("SSL", keyStore, "test3");

        assertThat(sslContext).isNotNull();
        assertThat(sslContext.getProtocol()).isEqualTo("SSL");
    }

    // Note: Good case is tested via integration test

    private static KeyStore createDefaultKeyStore(String password) throws KeyStoreException {
        return KeyStore.Builder.newInstance(KeyStore.getDefaultType(), null, new PasswordProtection(password.toCharArray())).getKeyStore();
    }
}