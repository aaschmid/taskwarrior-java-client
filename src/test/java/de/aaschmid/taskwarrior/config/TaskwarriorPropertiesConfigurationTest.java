package de.aaschmid.taskwarrior.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class TaskwarriorPropertiesConfigurationTest {

    @TempDir
    Path tempDir;

    private TaskwarriorPropertiesConfiguration configFor(String... lines) throws Exception {
        Path properties = Files.createFile(tempDir.resolve("taskwarrior.properties"));
        Files.write(properties, Arrays.asList(lines));
        return new TaskwarriorPropertiesConfiguration(properties.toUri().toURL());
    }

    @Test
    void shouldThrowNullPointerExceptionIfUrlIsNull() {
        assertThatThrownBy(() -> new TaskwarriorPropertiesConfiguration(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'propertiesUrl' must not be null.");
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionIfUrlIsNoExistingFile() throws Exception {
        URL url = new URL("file:/tmp/no-file");
        assertThatThrownBy(() -> new TaskwarriorPropertiesConfiguration(url))
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessage("Cannot read 'file:/tmp/no-file'. Check file existence and permissions.");
    }

    @Test
    void shouldSuccessfullyParseEmptyFile() {
        assertThatCode(this::configFor).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionIfPropertyIsNotAvailable() {
        assertThatThrownBy(() -> configFor().getServerHost())
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessageMatching("Could not find property with key 'taskwarrior.server.host' in '.*/taskwarrior.properties'.");
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionForInvalidHost() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor("taskwarrior.server.host=host.unknown");
        assertThatThrownBy(config::getServerHost)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessage("Cannot resolve host address 'host.unknown'.")
                .hasCauseInstanceOf(UnknownHostException.class);
    }

    @Test
    void shouldSuccessfullyParseValidHost() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor("taskwarrior.server.host=localhost");
        assertThat(config.getServerHost()).isEqualTo(InetAddress.getByName("localhost"));
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionForInvalidPort() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor("taskwarrior.server.port=invalid");
        assertThatThrownBy(config::getServerPort)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessage("Cannot resolve port 'invalid' because it is not a parsable.")
                .hasCauseInstanceOf(NumberFormatException.class);
    }

    @Test
    void shouldSuccessfullyParseValidPort() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor("taskwarrior.server.port=12345");
        assertThat(config.getServerPort()).isEqualTo(12345);
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionForInvalidCaCert() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor("taskwarrior.ssl.cert.ca.file=not-existing.cert.pem");
        assertThatThrownBy(config::getCaCertFile)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessage("CA certificate file 'not-existing.cert.pem' does not exist.");
    }

    @Test
    void shouldSuccessfullyParseValidCaCert() throws Exception {
        Path caCertFile = Files.createFile(tempDir.resolve("ca.cert.pem"));
        TaskwarriorPropertiesConfiguration config = configFor("taskwarrior.ssl.cert.ca.file=" + caCertFile.toAbsolutePath());
        assertThat(config.getCaCertFile()).isEqualTo(caCertFile.toFile());
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionForInvalidKeyCert() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor("taskwarrior.ssl.cert.key.file=not-existing.cert.pem");
        assertThatThrownBy(config::getPrivateKeyCertFile)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessage("Private key certificate file 'not-existing.cert.pem' does not exist.");
    }

    @Test
    void shouldSuccessfullyParseValidKeyCert() throws Exception {
        Path userCertFile = Files.createFile(tempDir.resolve("user.cert.pem"));
        TaskwarriorPropertiesConfiguration config = configFor("taskwarrior.ssl.cert.key.file=" + userCertFile.toAbsolutePath());
        assertThat(config.getPrivateKeyCertFile()).isEqualTo(userCertFile.toFile());
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionForInvalidUserKey() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor("taskwarrior.ssl.private.key.file=not-existing.key.pem");
        assertThatThrownBy(config::getPrivateKeyFile)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessage("Private key file 'not-existing.key.pem' does not exist.");
    }

    @Test
    void shouldSuccessfullyParseValidUserKey() throws Exception {
        Path userKeyFile = Files.createFile(tempDir.resolve("user.key.pem"));
        TaskwarriorPropertiesConfiguration config = configFor("taskwarrior.ssl.private.key.file=" + userKeyFile.toAbsolutePath());
        assertThat(config.getPrivateKeyFile()).isEqualTo(userKeyFile.toFile());
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionForInvalidAuthenticationKey() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor(
                "taskwarrior.auth.organisation=Org",
                "taskwarrior.auth.key=invalid",
                "taskwarrior.auth.user=User"
        );
        assertThatThrownBy(config::getAuthentication)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessage("Authentication key 'invalid' is not a parsable UUID.");
    }

    @Test
    void shouldSuccessfullyParseValidAuthentication() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor(
                "taskwarrior.auth.organisation=Org",
                "taskwarrior.auth.key=12c98b25-9234-4a7d-824a-531f603b12fa",
                "taskwarrior.auth.user=User"
        );

        assertThat(config.getAuthentication().getOrganisation()).isEqualTo("Org");
        assertThat(config.getAuthentication().getKey()).isEqualTo(UUID.fromString("12c98b25-9234-4a7d-824a-531f603b12fa"));
        assertThat(config.getAuthentication().getUser()).isEqualTo("User");
    }
}