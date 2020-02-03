package de.aaschmid.taskwarrior.message;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.jar.Manifest;

import static java.lang.String.format;

class ManifestHelper {

    static String getImplementationTitleAndVersionFromManifest(Class<?> clazzFileInJar, String fallback) {
        return Optional.of(clazzFileInJar)
                .flatMap(ManifestHelper::getJarUrlForClass)
                .flatMap(ManifestHelper::getManifest)
                .map(manifest -> format(
                        "%s %s",
                        getManifestAttributeValue(manifest, "Implementation-Title").orElse(""),
                        getManifestAttributeValue(manifest, "Implementation-Version").orElse("")
                ))
                .orElse(fallback);
    }

    private static Optional<String> getJarUrlForClass(Class<?> clazz) {
        return getResourceUrlForClass(clazz).map(URL::toString)
                .filter(p -> p.startsWith("jar:"))
                .map(p -> p.substring(0, p.lastIndexOf("!") + 1));
    }

    private static Optional<URL> getResourceUrlForClass(Class<?> clazz) {
        String className = clazz.getSimpleName() + ".class";
        return Optional.ofNullable(clazz.getResource(className));
    }

    private static Optional<Manifest> getManifest(String jarUrl) {
        try {
            return Optional.of(new Manifest(new URL(jarUrl + "/META-INF/MANIFEST.MF").openStream()));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static Optional<String> getManifestAttributeValue(Manifest manifest, String manifestAttributeKey) {
        return Optional.ofNullable(manifest.getMainAttributes().getValue(manifestAttributeKey));
    }
}
