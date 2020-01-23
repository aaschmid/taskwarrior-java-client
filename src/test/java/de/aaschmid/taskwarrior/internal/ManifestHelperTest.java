package de.aaschmid.taskwarrior.internal;

import de.aaschmid.taskwarrior.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import static de.aaschmid.taskwarrior.internal.ManifestHelper.getImplementationVersionFromManifest;
import static org.assertj.core.api.Assertions.assertThat;

class ManifestHelperTest {

    // Note: This test is not perfect and needs to be adjusted on every version update
    @IntegrationTest
    void getImplementationVersionFromManifest_shouldReturnCurrentVersionFromJarFile() {
        assertThat(getImplementationVersionFromManifest("test")).isEqualTo("1.0-SNAPSHOT");
    }

    @Test
    void getImplementationVersionFromManifest_shouldReturnFallbackStringWithoutJarFile() {
        assertThat(getImplementationVersionFromManifest("fallback")).isEqualTo("fallback");
    }
}