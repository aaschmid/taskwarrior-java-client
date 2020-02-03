package de.aaschmid.taskwarrior.config;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.UUID;

import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.AUTH_KEY;
import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.ORGANIZATION;
import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.SERVER_HOST;
import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.SERVER_PORT;
import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.SSL_CERT_CA_FILE;
import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.SSL_PRIVATE_KEY_CERT_FILE;
import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.SSL_PRIVATE_KEY_FILE;
import static de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration.PropertyKey.USER;
import static java.util.Objects.requireNonNull;

/**
 * {@link TaskwarriorConfiguration} based on a specified properties file
 */
class TaskwarriorPropertiesConfiguration implements TaskwarriorConfiguration {

    enum PropertyKey {
        AUTH_KEY("taskwarrior.auth.key"),
        ORGANIZATION("taskwarrior.auth.organization"),
        USER("taskwarrior.auth.user"),

        SERVER_HOST("taskwarrior.server.host"),
        SERVER_PORT("taskwarrior.server.port"),

        SSL_CERT_CA_FILE("taskwarrior.ssl.cert.ca.file"),
        SSL_PRIVATE_KEY_CERT_FILE("taskwarrior.ssl.cert.key.file"),
        SSL_PRIVATE_KEY_FILE("taskwarrior.ssl.private.key.file");

        public final String key;

        PropertyKey(String key) {
            this.key = key;
        }
    }

    private final URL propertiesUrl;
    private final Properties taskwarriorProperties;

    TaskwarriorPropertiesConfiguration(URL propertiesUrl) {
        this.propertiesUrl = requireNonNull(propertiesUrl, "'propertiesUrl' must not be null.");

        this.taskwarriorProperties = new Properties();
        try {
            taskwarriorProperties.load(propertiesUrl.openStream());
        } catch (IOException e) {
            throw new TaskwarriorConfigurationException(e, "Cannot read '%s'. Check file existence and permissions.", propertiesUrl);
        }
    }

    @Override
    public File getCaCertFile() {
        return getExistingFileFromProperty(SSL_CERT_CA_FILE.key, "CA certificate");
    }

    @Override
    public File getPrivateKeyCertFile() {
        return getExistingFileFromProperty(SSL_PRIVATE_KEY_CERT_FILE.key, "Private key certificate");
    }

    @Override
    public File getPrivateKeyFile() {
        return getExistingFileFromProperty(SSL_PRIVATE_KEY_FILE.key, "Private key");
    }

    @Override
    public InetAddress getServerHost() {
        String host = getExistingProperty(SERVER_HOST.key);
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new TaskwarriorConfigurationException(e, "Cannot resolve host address '%s'.", host);
        }
    }

    @Override
    public int getServerPort() {
        String port = getExistingProperty(SERVER_PORT.key);
        try {
            return Integer.decode(port);
        } catch (NumberFormatException e) {
            throw new TaskwarriorConfigurationException(e, "Cannot resolve port '%s' because it is not a parsable.", port);
        }
    }

    @Override
    public String getOrganization() {
        return getExistingProperty(ORGANIZATION.key);
    }

    @Override
    public UUID getAuthKey() {
        String key = getExistingProperty(AUTH_KEY.key);
        try {
            return UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            throw new TaskwarriorConfigurationException(e, "Authentication key '%s' is not a parsable UUID.", key);
        }
    }

    @Override
    public String getUser() {
        return getExistingProperty(USER.key);
    }

    private String getExistingProperty(String key) {
        String value = taskwarriorProperties.getProperty(key);
        if (value == null) {
            throw new TaskwarriorConfigurationException("Could not find property with key '%s' in '%s'.", key, propertiesUrl);
        }
        return value;
    }

    private File getExistingFileFromProperty(String key, String fileErrorText) {
        String property = getExistingProperty(key);

        File result = new File(property);
        if (!result.exists()) {
            throw new TaskwarriorConfigurationException("%s file '%s' does not exist.", fileErrorText, property);
        }
        return result;
    }
}
