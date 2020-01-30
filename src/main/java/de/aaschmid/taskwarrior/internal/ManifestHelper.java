package de.aaschmid.taskwarrior.internal;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.jar.Manifest;

public class ManifestHelper {

    public static String getImplementationVersionFromManifest(Class<?> clazzFileInJar, String fallbackVersion) {
        // @formatter:off
        return Optional.of(clazzFileInJar)
                .flatMap(ManifestHelper::getJarUrlForClass)
                .flatMap(u -> getManifestAttributeValue(u, "Implementation-Version"))
                .orElse(fallbackVersion);
        // @formatter:on
    }

    public static String getImplementationVersionFromManifest(String fallbackVersion) {
        return getImplementationVersionFromManifest(ManifestHelper.class, fallbackVersion);
    }

    public static Optional<URL> getResourceUrlForClass(Class<?> clazz) {
        String className = clazz.getSimpleName() + ".class";
        return Optional.ofNullable(clazz.getResource(className));
    }

    public static Optional<String> getJarUrlForClass(Class<?> clazz) {
        // @formatter:off
        return getResourceUrlForClass(clazz)
                .map(URL::toString)
                .filter(p -> p.startsWith("jar:"))
                .map(p -> p.substring(0, p.lastIndexOf("!") + 1));
        // @formatter:on
    }

    public static Optional<String> getManifestAttributeValue(String jarUrl, String manifestAttributeKey) {
        try {
            Manifest manifest = new Manifest(new URL(jarUrl + "/META-INF/MANIFEST.MF").openStream());
            return Optional.ofNullable(manifest.getMainAttributes().getValue(manifestAttributeKey));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
