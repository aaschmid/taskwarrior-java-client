package de.aaschmid.taskwarrior.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URL;

import org.junit.jupiter.api.Test;

public class TaskwarriorPropertiesConfigurationTest {

    @Test
    public void testTaskwarriorPropertiesConfigurationShouldThrowNullPointerExceptionIfUrlIsNull() {
        assertThatThrownBy(() -> new TaskwarriorPropertiesConfiguration(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testTaskwarriorPropertiesConfigurationShouldThrowTaskwarriorConfigurationExceptionIfUrlIsNoFile()
            throws Exception {
        URL url = new URL("file:/tmp/noFile");
        assertThatThrownBy(() -> new TaskwarriorPropertiesConfiguration(url))
                .isInstanceOf(TaskwarriorConfigurationException.class);
    }
}