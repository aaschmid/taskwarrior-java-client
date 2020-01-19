package de.aaschmid.taskwarrior.test;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Tag("integration-test")
public @interface IntegrationTest {
}
