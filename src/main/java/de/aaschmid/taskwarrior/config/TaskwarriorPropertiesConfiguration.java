package de.aaschmid.taskwarrior.config;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.UUID;

/** {@link TaskwarriorConfiguration} based on a specified properties file */
public class TaskwarriorPropertiesConfiguration implements TaskwarriorConfiguration {

    private static final String PROPERTY_TASKWARRIOR_SSL_CERT_CA_FILE = "taskwarrior.ssl.cert.ca.file";
    private static final String PROPERTY_TASKWARRIOR_SSL_CERT_KEY_FILE = "taskwarrior.ssl.cert.key.file";
    private static final String PROPERTY_TASKWARRIOR_SSL_PUBLIC_KEY_FILE = "taskwarrior.ssl.private.key.file";
    private static final String PROPERTY_TASKWARRIOR_SERVER_HOST = "taskwarrior.server.host";
    private static final String PROPERTY_TASKWARRIOR_SERVER_PORT = "taskwarrior.server.port";
    private static final String PROPERTY_TASKWARRIOR_AUTH_KEY = "taskwarrior.auth.key";
    private static final String PROPERTY_TASKWARRIOR_AUTH_USER = "taskwarrior.auth.user";
    private static final String PROPERTY_TASKWARRIOR_AUTH_ORGANISATION = "taskwarrior.auth.organisation";

    private final URL propertiesUrl;
    private final Properties taskwarriorProperties;

    public TaskwarriorPropertiesConfiguration(URL propertiesUrl) {
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
        return getExistingFileFromProperty(PROPERTY_TASKWARRIOR_SSL_CERT_CA_FILE, "CA certificate");
    }

    @Override
    public File getPrivateKeyCertFile() {
        return getExistingFileFromProperty(PROPERTY_TASKWARRIOR_SSL_CERT_KEY_FILE, "Private key certificate");
    }

    @Override
    public File getPrivateKeyFile() {
        return getExistingFileFromProperty(PROPERTY_TASKWARRIOR_SSL_PUBLIC_KEY_FILE, "Private key");
    }

    @Override
    public InetAddress getServerHost() {
        String host = getExistingProperty(PROPERTY_TASKWARRIOR_SERVER_HOST);
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new TaskwarriorConfigurationException(e, "Cannot resolve host address '%s'.", host);
        }
    }

    @Override
    public int getServerPort() {
        String port = getExistingProperty(PROPERTY_TASKWARRIOR_SERVER_PORT);
        try {
            return Integer.decode(port);
        } catch (NumberFormatException e) {
            throw new TaskwarriorConfigurationException(e, "Cannot resolve port '%s' because it is not a parsable.", port);
        }
    }

    @Override
    public TaskwarriorAuthentication getAuthentication() {
        String org = getExistingProperty(PROPERTY_TASKWARRIOR_AUTH_ORGANISATION);
        UUID key = getExistingAuthenticationKey();
        String user = getExistingProperty(PROPERTY_TASKWARRIOR_AUTH_USER);
        return new TaskwarriorAuthentication(org, key, user);
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

    private UUID getExistingAuthenticationKey() {
        String key = getExistingProperty(TaskwarriorPropertiesConfiguration.PROPERTY_TASKWARRIOR_AUTH_KEY);
        try {
            return UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            throw new TaskwarriorConfigurationException(e, "Authentication key '%s' is not a parsable UUID.", key);
        }
    }
}
