package de.aaschmid.taskwarrior.message;

import de.aaschmid.taskwarrior.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import static de.aaschmid.taskwarrior.message.ManifestHelper.getImplementationTitleAndVersionFromManifest;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ManifestHelperTest {

    // Note: This test is not perfect and needs to be adjusted on every version update
    @Test
    void getImplementationTitleAndVersionFromManifest_shouldReturnCurrentVersionFromJarFile() {
        assertThat(getImplementationTitleAndVersionFromManifest(ManifestHelper.class, "fallback"))
                .isEqualTo("taskwarrior-java-client 1.0-SNAPSHOT");
    }

    @Test
    void getImplementationTitleAndVersionFromManifest_shouldReturnFallbackStringWithoutJarFile() {
        assertThat(getImplementationTitleAndVersionFromManifest(ManifestHelperTest.class, "fallback")).isEqualTo("fallback");
    }
}
