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
    void getInstance_shouldThrowNullPointerExceptionIfProtocolIsNull() {
        assertThatThrownBy(() -> SslContextFactory.getInstance(null, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'protocol' must not be null.");
    }

    @Test
    void getInstance_shouldThrowNullPointerExceptionIfKeyStoreIsNull() {
        assertThatThrownBy(() -> SslContextFactory.getInstance("SSL", null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'keyStore' must not be null.");
    }

    @Test
    void getInstance_shouldThrowNullPointerExceptionIfKeyStorePasswordIsNull() throws KeyStoreException {
        KeyStore keyStore = createDefaultKeyStore("test1");
        assertThatThrownBy(() -> SslContextFactory.getInstance("SSL", keyStore, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'keyStorePassword' must not be null.");
    }

    @Test
    void getInstance_shouldThrowTaskwarriorSslContextExceptionIfProtocolIsInvalid() throws KeyStoreException {
        KeyStore keyStore = createDefaultKeyStore("test2");
        assertThatThrownBy(() -> SslContextFactory.getInstance("invalid", keyStore, "test"))
                .isInstanceOf(TaskwarriorSslContextException.class)
                .hasMessage("Cannot create SSL context for protocol 'invalid'.")
                .hasCauseInstanceOf(NoSuchAlgorithmException.class);
    }

    @Test
    void getInstance_should() throws KeyStoreException {
        KeyStore keyStore = createDefaultKeyStore("test3");
        SSLContext sslContext = SslContextFactory.getInstance("SSL", keyStore, "test");

        assertThat(sslContext).isNotNull();
    }

    private static KeyStore createDefaultKeyStore(String password) throws KeyStoreException {
        return KeyStore.Builder.newInstance(KeyStore.getDefaultType(), null, new PasswordProtection(password.toCharArray())).getKeyStore();
    }
}