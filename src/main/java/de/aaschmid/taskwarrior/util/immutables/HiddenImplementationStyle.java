package de.aaschmid.taskwarrior.util.immutables;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.immutables.value.Value;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS) // Make it class retention for incremental compilation
@Value.Style(
        builderVisibility = Value.Style.BuilderVisibility.PACKAGE,
        depluralize = true,
        get = { "is*", "get*" },
        jdkOnly = true,
        privateNoargConstructor = true,
        visibility = Value.Style.ImplementationVisibility.PRIVATE
)
public @interface HiddenImplementationStyle {
}
