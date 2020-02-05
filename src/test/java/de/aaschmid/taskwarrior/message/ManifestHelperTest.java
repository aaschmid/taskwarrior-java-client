package de.aaschmid.taskwarrior.message;

import de.aaschmid.taskwarrior.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import static de.aaschmid.taskwarrior.message.ManifestHelper.getAttributeValuesFromManifest;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ManifestHelperTest {

    private static final String IMPL_TITLE = "Implementation-Title";
    private static final String IMPL_VENDOR = "Implementation-Vendor";
    private static final String LICENSE = "License";
    private static final String MANIFEST_VERSION = "Manifest-Version";

    @Test
    void getAttributeValuesFromManifest_shouldReturnEmptyListIfNoAttributeAreGiven() {
        assertThat(getAttributeValuesFromManifest(ManifestHelper.class)).isEmpty();
    }

    @Test
    void getAttributeValuesFromManifest_shouldReturnFoundAttributeIfClassIsPartOfJarFile() {
        assertThat(getAttributeValuesFromManifest(ManifestHelper.class, IMPL_TITLE)).containsOnly("taskwarrior-java-client");
    }

    @Test
    void getAttributeValuesFromManifest_shouldReturnNoAttributesIfClassIsNotPartOfJarFile() {
        assertThat(getAttributeValuesFromManifest(ManifestHelperTest.class, IMPL_TITLE)).isEmpty();
    }

    @Test
    void getAttributeValuesFromManifest_shouldReturnFoundAttributeIfInnerClassIsPartOfJarFile() {
        assertThat(getAttributeValuesFromManifest(TaskwarriorRequestHeader.MessageType.class, LICENSE))
                .containsExactly("Apache License v2.0, January 2004");
    }

    @Test
    void getAttributeValuesFromManifest_shouldReturnEmptyListIfNoAttributeNameIsFoundInJarFile() {
        assertThat(getAttributeValuesFromManifest(ManifestHelper.class, "unknown")).isEmpty();
    }

    @Test
    void getAttributeValuesFromManifest_shouldReturnFoundAttributesIfClassIsPartOfJarFile() {
        assertThat(getAttributeValuesFromManifest(ManifestHelper.class, IMPL_TITLE, IMPL_VENDOR))
                .containsExactly("taskwarrior-java-client", "Andreas Schmid");
    }

    @Test
    void getAttributeValuesFromManifest_shouldReturnEmptyListIfNoAttributeNamesAreFoundInJarFile() {
        assertThat(getAttributeValuesFromManifest(ManifestHelper.class, "unknown1", "unknown2", "unknown3")).isEmpty();
    }

    @Test
    void getAttributeValuesFromManifest_shouldReturnOnlyFoundAttributesInJarFile() {
        assertThat(getAttributeValuesFromManifest(ManifestHelper.class, "x", IMPL_TITLE, "unknown", MANIFEST_VERSION))
                .containsExactly("taskwarrior-java-client", "1.0");
    }
}
