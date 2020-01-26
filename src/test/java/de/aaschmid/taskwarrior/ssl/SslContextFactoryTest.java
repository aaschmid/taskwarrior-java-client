package de.aaschmid.taskwarrior.ssl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;

import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

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
        KeyStore keyStore = KeyStore.Builder.newInstance(KeyStore.getDefaultType(), null, new PasswordProtection("test".toCharArray())).getKeyStore();
        assertThatThrownBy(() -> SslContextFactory.getInstance("SSL", keyStore, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'keyStorePassword' must not be null.");
    }

    @Test
    void getInstance_shouldThrowTaskwarriorSslContextExceptionIfProtocolIsInvalid() throws KeyStoreException {
        KeyStore keyStore = KeyStore.Builder.newInstance(KeyStore.getDefaultType(), null, new PasswordProtection("test".toCharArray())).getKeyStore();
        assertThatThrownBy(() -> SslContextFactory.getInstance("invalid", keyStore, "test"))
                .isInstanceOf(TaskwarriorSslContextException.class)
                .hasMessage("Cannot create SSL context for protocol 'invalid'.")
                .hasCauseInstanceOf(NoSuchAlgorithmException.class);
    }

    @Test
    void getInstance_should() throws KeyStoreException {
        KeyStore keyStore = KeyStore.Builder.newInstance(KeyStore.getDefaultType(), null, new PasswordProtection("test".toCharArray())).getKeyStore();
        SSLContext sslContext = SslContextFactory.getInstance("SSL", keyStore, "test");

        assertThat(sslContext).isNotNull();
    }
}