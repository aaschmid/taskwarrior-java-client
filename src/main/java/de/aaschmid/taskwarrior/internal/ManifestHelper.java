package de.aaschmid.taskwarrior.internal;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Manifest;

public class ManifestHelper {

    public static URL getResourceUrlForClass(Class<?> clazz) {
        String className = clazz.getSimpleName() + ".class";
        return clazz.getResource(className);
    }

    public static String getJarUrlForClass(Class<?> clazz) {
        URL resource = getResourceUrlForClass(clazz);
        if (resource == null) {
            return null;
        }
        String classPath = resource.toString();
        if (classPath.startsWith("jar:")) {
            return classPath.substring(0, classPath.lastIndexOf("!") + 1);
        }
        return null;
    }

    public static String getManifestAttributeValue(String jarUrl, String manifestAttributeKey) {
        try {
            Manifest manifest = new Manifest(new URL(jarUrl + "/META-INF/MANIFEST.MF").openStream());
            return manifest.getMainAttributes().getValue(manifestAttributeKey);

        } catch (IOException e) {
            // TODO log this
            return null;
        }
    }

    public static String getImplementationVersionFromManifest(String fallbackVersion) {
        String jarUrl = getJarUrlForClass(ManifestHelper.class);
        if (jarUrl == null) {
            return fallbackVersion;
        }
        String result = getManifestAttributeValue(jarUrl, "Implementation-Version");
        if (result == null) {
            return fallbackVersion;
        }
        return result;
    }
}
