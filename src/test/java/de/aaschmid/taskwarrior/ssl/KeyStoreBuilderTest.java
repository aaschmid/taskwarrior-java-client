package de.aaschmid.taskwarrior.ssl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.net.ssl.X509KeyManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

class KeyStoreBuilderTest {

    @TempDir
    Path tempDir;

    private KeyStoreBuilder keyStoreBuilder = new KeyStoreBuilder();

    @Test
    void withKeyStoreProtection_shouldThrowNullPointerExceptionOnNull() {
        assertThatThrownBy(() -> keyStoreBuilder.withKeyStoreProtection(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'keyStoreProtection' must not be null.");
    }

    @Test
    void withKeyStoreProtection_shouldNotThrowOnNonNull() {
        ProtectionParameter protection = new PasswordProtection("test".toCharArray());

        assertThatCode(() -> keyStoreBuilder.withKeyStoreProtection(protection)).doesNotThrowAnyException();
    }

    @Test
    void withCaCertFile_shouldThrowNullPointerExceptionOnNull() {
        assertThatThrownBy(() -> keyStoreBuilder.withCaCertFile(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'caCertFile' must not be null.");
    }

    @Test
    void withCaCertFile_shouldThrowIllegalArgumentExceptionOnNotExistingFile() {
        assertThatThrownBy(() -> keyStoreBuilder.withCaCertFile(Paths.get("no-file").toFile()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CA certificate 'no-file' does not exist.");
    }

    @Test
    void withCaCertFile_shouldNotThrowOnExistingFile() throws IOException {
        Path caCertFile = Files.createFile(tempDir.resolve("ca.cert.pem"));
        assertThatCode(() -> keyStoreBuilder.withCaCertFile(caCertFile.toFile())).doesNotThrowAnyException();
    }

    @Test
    void withPrivateKeyCertFile_shouldThrowNullPointerExceptionOnNull() {
        assertThatThrownBy(() -> keyStoreBuilder.withPrivateKeyCertFile(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'privateKeyCertFile' must not be null.");
    }

    @Test
    void withPrivateKeyCertFile_shouldThrowIllegalArgumentExceptionOnNotExistingFile() {
        assertThatThrownBy(() -> keyStoreBuilder.withPrivateKeyCertFile(Paths.get("no-file").toFile()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Private key certificate 'no-file' does not exist.");
    }

    @Test
    void withPrivateKeyCertFile_shouldNotThrowOnExistingFile() throws IOException {
        Path privateKeyCertFile = Files.createFile(tempDir.resolve("key.cert.pem"));
        assertThatCode(() -> keyStoreBuilder.withPrivateKeyCertFile(privateKeyCertFile.toFile())).doesNotThrowAnyException();
    }

    @Test
    void withPrivateKeyFile_shouldThrowNullPointerExceptionOnNull() {
        assertThatThrownBy(() -> keyStoreBuilder.withPrivateKeyFile(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'privateKeyFile' must not be null.");
    }

    @Test
    void withPrivateKeyFile_shouldThrowIllegalArgumentExceptionOnNotExistingFile() {
        assertThatThrownBy(() -> keyStoreBuilder.withPrivateKeyFile(Paths.get("no-file").toFile()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Private key 'no-file' does not exist.");
    }

    @Test
    void withPrivateKeyFile_shouldNotThrowOnExistingFile() throws IOException {
        Path privateKeyFile = Files.createFile(tempDir.resolve("key.pem"));
        assertThatCode(() -> keyStoreBuilder.withPrivateKeyFile(privateKeyFile.toFile())).doesNotThrowAnyException();
    }

    @Test
    void build_shouldThrowTaskwarriorKeyStoreExceptionIfCaCertificateIsInvalid() throws IOException {
        Path caCertFile = Files.write(tempDir.resolve("ca.cert.pem"), "invalid cert".getBytes());
        KeyStoreBuilder builder = keyStoreBuilder.withCaCertFile(caCertFile.toFile());

        assertThatThrownBy(builder::build)
                .isInstanceOf(TaskwarriorKeyStoreException.class)
                .hasMessage("Could not generate certificates for '" + caCertFile.toAbsolutePath() + "'.")
                .hasCauseInstanceOf(CertificateException.class);
    }

    @Test
    void build_shouldThrowTaskwarriorKeyStoreExceptionIfPrivateKeyCertificateIsInvalid() throws IOException {
        Path caCertFile = Files.createFile(tempDir.resolve("ca.cert.pem"));
        Path privateKeyCertFile = Files.write(Files.createFile(tempDir.resolve("key.cert.pem")), "invalid cert".getBytes());
        KeyStoreBuilder builder = keyStoreBuilder
                .withCaCertFile(caCertFile.toFile())
                .withPrivateKeyCertFile(privateKeyCertFile.toFile());

        assertThatThrownBy(builder::build)
                .isInstanceOf(TaskwarriorKeyStoreException.class)
                .hasMessage("Could not generate certificates for '" + privateKeyCertFile.toAbsolutePath() + "'.")
                .hasCauseInstanceOf(CertificateException.class);
    }

    @Test
    void build_shouldTaskwarriorKeyStoreExceptionIfPrivateKeyIsInvalid() throws IOException {
        Path caCertFile = Files.createFile(tempDir.resolve("ca.cert.pem"));
        Path privateKeyCertFile = Files.createFile(tempDir.resolve("key.cert.pem"));
        Path privateKeyFile = Files.write(Files.createFile(tempDir.resolve("key.der")), "invalid cert".getBytes());
        KeyStoreBuilder builder = keyStoreBuilder
                .withCaCertFile(caCertFile.toFile())
                .withPrivateKeyCertFile(privateKeyCertFile.toFile())
                .withPrivateKeyFile(privateKeyFile.toFile());

        assertThatThrownBy(builder::build)
                .isInstanceOf(TaskwarriorKeyStoreException.class)
                .hasMessage("Could not generate private key for '" + privateKeyFile.toAbsolutePath() + "'.")
                .hasCauseInstanceOf(InvalidKeySpecException.class);
    }

    // Note: Good case is tested via integration test
}