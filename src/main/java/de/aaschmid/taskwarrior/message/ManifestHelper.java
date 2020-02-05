package de.aaschmid.taskwarrior.message;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

class ManifestHelper {

    static List<String> getAttributeValuesFromManifest(Class<?> clazzFileInJar, String... manifestAttributeNames) {
        return Optional.of(clazzFileInJar)
                .flatMap(ManifestHelper::getJarUrlForClass)
                .flatMap(ManifestHelper::getManifest)
                .map(manifest -> getManifestAttributeValue(manifest, manifestAttributeNames))
                .orElse(emptyList());
    }

    private static Optional<String> getJarUrlForClass(Class<?> clazz) {
        return getResourceUrlForClass(clazz).map(URL::toString)
                .filter(p -> p.startsWith("jar:"))
                .map(p -> p.substring(0, p.lastIndexOf("!") + 1));
    }

    private static Optional<Manifest> getManifest(String jarUrl) {
        try {
            return Optional.of(new Manifest(new URL(jarUrl + "/META-INF/MANIFEST.MF").openStream()));
        } catch (IOException e) {
            return Optional.empty(); // no MANIFEST.MF in jar file
        }
    }

    private static List<String> getManifestAttributeValue(Manifest manifest, String... manifestAttributeNames) {
        Attributes mainAttributes = manifest.getMainAttributes();
        return Arrays.stream(manifestAttributeNames).map(mainAttributes::getValue).filter(Objects::nonNull).collect(toList());
    }

    private static Optional<URL> getResourceUrlForClass(Class<?> clazz) {
        String className = getMostOuterDeclaringClass(clazz).getSimpleName() + ".class";
        return Optional.ofNullable(clazz.getResource(className));
    }

    private static Class<?> getMostOuterDeclaringClass(Class<?> clazz) {
        Class<?> declaringClass = clazz;
        while (declaringClass.getDeclaringClass() != null) {
            declaringClass = declaringClass.getDeclaringClass();
        }
        return declaringClass;
    }
}
