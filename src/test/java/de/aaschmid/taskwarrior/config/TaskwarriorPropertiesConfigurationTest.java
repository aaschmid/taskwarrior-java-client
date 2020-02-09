package de.aaschmid.taskwarrior.config;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.AUTH_KEY;
import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.ORGANIZATION;
import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.SERVER_HOST;
import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.SERVER_PORT;
import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.SSL_CERT_CA_FILE;
import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.SSL_PRIVATE_KEY_CERT_FILE;
import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.SSL_PRIVATE_KEY_FILE;
import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.USER;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskwarriorPropertiesConfigurationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldThrowNullPointerExceptionIfUrlIsNull() {
        assertThatThrownBy(() -> new TaskwarriorPropertiesConfiguration(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'propertiesUrl' must not be null.");
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionIfUrlReferencesNotExistingFile() throws Exception {
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
    void shouldThrowTaskwarriorConfigurationExceptionIfHostIsMissing() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor();
        assertThatThrownBy(config::getServerHost)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessageMatching(format("Could not find property with key '%s' in '.*/taskwarrior.properties'.", SERVER_HOST.key));
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionForInvalidHost() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor(prop(SERVER_HOST, "host.unknown"));
        assertThatThrownBy(config::getServerHost)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessage("Cannot resolve host address 'host.unknown'.")
                .hasCauseInstanceOf(UnknownHostException.class);
    }

    @Test
    void shouldSuccessfullyParseValidHost() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor(prop(SERVER_HOST, "localhost"));
        assertThat(config.getServerHost()).isEqualTo(InetAddress.getByName("localhost"));
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionIfPortIsMissing() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor();
        assertThatThrownBy(config::getServerPort)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessageMatching(format("Could not find property with key '%s' in '.*/taskwarrior.properties'.", SERVER_PORT.key));
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionForInvalidPort() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor(prop(SERVER_PORT, "invalid"));
        assertThatThrownBy(config::getServerPort)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessage("Cannot resolve port 'invalid' because it is not a parsable.")
                .hasCauseInstanceOf(NumberFormatException.class);
    }

    @Test
    void shouldSuccessfullyParseValidPort() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor(prop(SERVER_PORT, "12345"));
        assertThat(config.getServerPort()).isEqualTo(12345);
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionIfCaCertIsMissing() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor();
        assertThatThrownBy(config::getCaCertFile)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessageMatching(format("Could not find property with key '%s' in '.*/taskwarrior.properties'.", SSL_CERT_CA_FILE.key));
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionForInvalidCaCert() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor(prop(SSL_CERT_CA_FILE, "not-existing.cert.pem"));
        assertThatThrownBy(config::getCaCertFile)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessage("CA certificate file 'not-existing.cert.pem' does not exist.");
    }

    @Test
    void shouldSuccessfullyParseValidCaCert() throws Exception {
        Path caCertFile = Files.createFile(tempDir.resolve("ca.cert.pem"));
        TaskwarriorPropertiesConfiguration config = configFor(prop(SSL_CERT_CA_FILE, caCertFile));
        assertThat(config.getCaCertFile()).isEqualTo(caCertFile.toFile());
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionIfPrivateKeyCertIsMissing() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor();
        assertThatThrownBy(config::getPrivateKeyCertFile)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessageMatching(format(
                        "Could not find property with key '%s' in '.*/taskwarrior.properties'.",
                        SSL_PRIVATE_KEY_CERT_FILE.key));
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionForInvalidPrivateKeyCert() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor(prop(SSL_PRIVATE_KEY_CERT_FILE, "not-existing.cert.pem"));
        assertThatThrownBy(config::getPrivateKeyCertFile)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessage("Private key certificate file 'not-existing.cert.pem' does not exist.");
    }

    @Test
    void shouldSuccessfullyParseValidPrivateKeyCert() throws Exception {
        Path userCertFile = Files.createFile(tempDir.resolve("user.cert.pem"));
        TaskwarriorPropertiesConfiguration config = configFor(prop(SSL_PRIVATE_KEY_CERT_FILE, userCertFile));
        assertThat(config.getPrivateKeyCertFile()).isEqualTo(userCertFile.toFile());
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionIfPrivateKeyIsMissing() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor();
        assertThatThrownBy(config::getPrivateKeyFile)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessageMatching(format(
                        "Could not find property with key '%s' in '.*/taskwarrior.properties'.",
                        SSL_PRIVATE_KEY_FILE.key));
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionForInvalidPrivateKey() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor(prop(SSL_PRIVATE_KEY_FILE, "not-existing.key.pem"));
        assertThatThrownBy(config::getPrivateKeyFile)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessage("Private key file 'not-existing.key.pem' does not exist.");
    }

    @Test
    void shouldSuccessfullyParseValidPrivateKey() throws Exception {
        Path userKeyFile = Files.createFile(tempDir.resolve("user.key.pem"));
        TaskwarriorPropertiesConfiguration config = configFor(prop(SSL_PRIVATE_KEY_FILE, userKeyFile));
        assertThat(config.getPrivateKeyFile()).isEqualTo(userKeyFile.toFile());
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionIfAuthenticationKeyIsMissing() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor();
        assertThatThrownBy(config::getAuthKey)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessageMatching(format("Could not find property with key '%s' in '.*/taskwarrior.properties'.", AUTH_KEY.key));
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionIfAuthenticationKeyIsInvalid() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor(prop(AUTH_KEY, "invalid"));
        assertThatThrownBy(config::getAuthKey)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessageMatching("Authentication key 'invalid' is not a parsable UUID.");
    }

    @Test
    void shouldSuccessfullyParseAuthenticationKey() throws Exception {
        UUID uuid = UUID.randomUUID();
        TaskwarriorPropertiesConfiguration config = configFor(prop(AUTH_KEY, uuid.toString()));
        assertThat(config.getAuthKey()).isEqualTo(uuid);
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionIfOrganizationIsMissing() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor();
        assertThatThrownBy(config::getOrganization)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessageMatching(format("Could not find property with key '%s' in '.*/taskwarrior.properties'.", ORGANIZATION.key));
    }

    @Test
    void shouldSuccessfullyReturnOrganization() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor(prop(ORGANIZATION, "org"));
        assertThat(config.getOrganization()).isEqualTo("org");
    }

    @Test
    void shouldThrowTaskwarriorConfigurationExceptionIfUserIsMissing() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor();
        assertThatThrownBy(config::getUser)
                .isInstanceOf(TaskwarriorConfigurationException.class)
                .hasMessageMatching(format("Could not find property with key '%s' in '.*/taskwarrior.properties'.", USER.key));
    }

    @Test
    void shouldSuccessfullyReturnUser() throws Exception {
        TaskwarriorPropertiesConfiguration config = configFor(prop(USER, "user"));
        assertThat(config.getUser()).isEqualTo("user");
    }

    private TaskwarriorPropertiesConfiguration configFor(String... lines) throws Exception {
        Path properties = Files.createFile(tempDir.resolve("taskwarrior.properties"));
        Files.write(properties, Arrays.asList(lines));
        return new TaskwarriorPropertiesConfiguration(properties.toUri().toURL());
    }

    private String prop(PropertyKey propertyKey, String value) {
        return format("%s=%s", propertyKey.key, value);
    }

    private String prop(PropertyKey propertyKey, Path value) {
        return prop(propertyKey, toPlatformIndependentAbsolutePath(value));
    }

    private String toPlatformIndependentAbsolutePath(Path path) {
        return path.toFile().getAbsolutePath().replaceAll("\\\\", "/");
    }
}