import com.github.spotbugs.SpotBugsTask
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "5.2.0"
    jacoco

    id("com.github.spotbugs") version "3.0.0"
    id("de.aaschmid.cpd") version "3.1"

    `maven-publish`
    signing
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}

base {
    archivesBaseName = "taskwarrior-java-client"
    description = "A Java client to communicate with a taskwarrior server (= taskd)."
    group = "de.aaschmid"
    version = "1.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("org.immutables:value:2.8.3")

    listOf("com.github.spotbugs:spotbugs-annotations:3.1.12", "org.immutables:value-annotations:2.8.3").forEach {
        compileOnly(it)
        testCompileOnly(it)
    }

    implementation("org.bouncycastle:bcpkix-jdk15on:1.64")

    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("org.assertj:assertj-core:3.14.0")
    testImplementation("org.mockito:mockito-junit-jupiter:3.2.4")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror", "-Xlint:-processing"))
    }

    withType<Jar> {
        from(project.rootDir) {
            include("LICENSE", "NOTICE")
            into("META-INF")
        }
    }

    jar {
        enabled = false
        manifest {
            val now = LocalDate.now()

            val title = base.archivesBaseName
            val vendor = "Andreas Schmid"
            val today = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val copyright = "${now.year} $vendor"

            attributes(
                    "Built-By" to "Gradle ${gradle.gradleVersion}",
                    "Built-Date" to today,
                    "Specification-Title" to title,
                    "Specification-Version" to archiveVersion.get(),
                    "Specification-Vendor" to vendor,
                    "Implementation-Title" to title,
                    "Implementation-Version" to archiveVersion.get(),
                    "Implementation-Vendor" to vendor,

                    "Automatic-Module-Name" to "de.aaschmid.taskwarrior.client",

                    "Issue-Tracker" to "https://github.com/aaschmid/taskwarrior-java-client/issues",
                    "Documentation-URL" to "https://github.com/aaschmid/taskwarrior-java-client",
                    "Copyright" to copyright,
                    "License" to "Apache License v2.0, January 2004"
            )
        }
    }

    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()
        minimize()
        relocate("org.bouncycastle", "de.aaschmid.taskwarrior.thirdparty.org.bouncycastle")
    }
    assemble {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }

    test {
        useJUnitPlatform {
            excludeTags("integration-test")
        }
    }

    val integTest = register<Test>("integTest") {
        shouldRunAfter(test)

        // run integration tests using jar file
        classpath = jar.get().outputs.files + sourceSets.test.get().runtimeClasspath
        testClassesDirs = sourceSets.test.get().output.classesDirs

        useJUnitPlatform {
            includeTags("integration-test")
        }
    }
    check { dependsOn(integTest) }

    val jacocoMerge = register("jacocoMerge", JacocoMerge::class) {
        executionData(withType(Test::class).toSet())
        dependsOn(test, integTest)
    }

    jacocoTestReport {
        executionData(jacocoMerge.get().destinationFile)
        reports {
            xml.isEnabled = true
            html.isEnabled = false
        }
        dependsOn(jacocoMerge)
    }

    cpdCheck {
        minimumTokenCount = 25
    }

    withType<SpotBugsTask> {
        reports {
            html.isEnabled = true
            xml.isEnabled = false
        }

        // Fixes https://github.com/spotbugs/spotbugs-gradle-plugin/issues/115
        val sourceSetName = name[8].toLowerCase() + name.substring(8).substring(1)
        classpath += sourceSets.named(sourceSetName).get().compileClasspath
    }
}

jacoco {
    toolVersion = "0.8.5"
}

cpd {
    toolVersion = "6.20.0"
    isIgnoreFailures = true
}

spotbugs {
    toolVersion = "3.1.12"
}

// -- publish and sign artifacts --------------------------------------------------------------------------------------
val isReleaseVersion by extra(!project.version.toString().endsWith("-SNAPSHOT"))

// username and password from gradle.properties otherwise empty
val sonatypeUsername by extra(findProperty("sonatypeUsername")?.toString() ?: "")
val sonatypePassword by extra(findProperty("sonatypePassword")?.toString() ?: "")

tasks.withType<GenerateModuleMetadata> {
    enabled = isReleaseVersion // signing of these artifacts causes failure for snapshot versions
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            // Not using `from(components["java"])` prevents from adding any shadowed dependency to resulting pom.xml
            shadow.component(this)
            artifact(tasks.get("javadocJar"))
            artifact(tasks.get("sourcesJar"))

            pom {
                packaging = "jar"

                name.set(base.archivesBaseName)
                description.set(project.description)
                url.set("https://github.com/aaschmid/taskwarrior-java-client")

                developers {
                    developer {
                        id.set("aaschmid")
                        name.set("Andreas Schmid")
                        email.set("service@aaschmid.de")
                    }
                }

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                scm {
                    connection.set("scm:git@github.com:aaschmid/taskwarrior-java-client.git")
                    developerConnection.set("scm:git@github.com:aaschmid/taskwarrior-java-client.git")
                    url.set("scm:git@github.com:aaschmid/taskwarrior-java-client.git")
                }
            }
        }
    }

    repositories {
        maven {
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            url = if (isReleaseVersion) releasesRepoUrl else snapshotRepoUrl

            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }
}

// requires gradle.properties, see http://www.gradle.org/docs/current/userguide/signing_plugin.html
signing {
    setRequired({ isReleaseVersion && gradle.taskGraph.hasTask("publish") })
    sign(publishing.publications["mavenJava"])
}
