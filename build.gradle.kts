import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import fr.xpdustry.toxopid.dsl.anukenJitpack
import fr.xpdustry.toxopid.dsl.mindustryDependencies
import fr.xpdustry.toxopid.spec.ModMetadata
import fr.xpdustry.toxopid.spec.ModPlatform
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("com.diffplug.spotless") version "6.11.0"
    id("net.kyori.indra") version "3.0.1"
    id("net.kyori.indra.publishing") version "3.0.1"
    id("net.kyori.indra.git") version "3.0.1"
    id("net.kyori.indra.licenser.spotless") version "3.0.1"
    id("net.ltgt.errorprone") version "3.0.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    // Testing :)
    id("fr.xpdustry.toxopid") version "3.1.0-SNAPSHOT"
}

val metadata = ModMetadata.fromJson(file("plugin.json").readText())
group = property("props.project-group").toString()
if (indraGit.headTag() == null && property("props.enable-snapshots").toString().toBoolean()) {
    metadata.version += "-SNAPSHOT"
}
version = metadata.version
description = metadata.description

toxopid {
    compileVersion.set("v" + metadata.minGameVersion)
    platforms.set(setOf(ModPlatform.HEADLESS))
}

repositories {
    mavenCentral()
    anukenJitpack()
    maven("https://maven.xpdustry.fr/snapshots") {
        mavenContent { snapshotsOnly() }
    }
}

val mindustryRuntime = configurations.register("mindustryRuntime")

dependencies {
    mindustryDependencies()
    compileOnly("fr.xpdustry:distributor-api:3.0.0-SNAPSHOT")

    val junit = "5.9.0"
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit")

    val checker = "3.27.0"
    compileOnly("org.checkerframework:checker-qual:$checker")
    testImplementation("org.checkerframework:checker-qual:$checker")

    // Static analysis
    annotationProcessor("com.uber.nullaway:nullaway:0.10.6")
    errorprone("com.google.errorprone:error_prone_core:2.17.0")
    mindustryRuntime.configure {
        invoke(
            group = "fr.xpdustry",
            name = "distributor-core",
            version = "3.0.0-SNAPSHOT",
            classifier = "plugin"
        )
    }
}

tasks.withType(JavaCompile::class.java).configureEach {
    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
        disable(
            "MissingSummary",
            "FutureReturnValueIgnored",
            "InlineMeSuggester",
            "EmptyCatch"
        )
        if (!name.contains("test", true)) {
            check("NullAway", CheckSeverity.ERROR)
            option("NullAway:AnnotatedPackages", project.property("props.root-package").toString())
            option("NullAway:TreatGeneratedAsUnannotated", true)
        }
    }
}

// Required for the GitHub actions
tasks.register("getArtifactPath") {
    doLast { println(tasks.shadowJar.get().archiveFile.get().toString()) }
}

// Relocates dependencies
val relocate = tasks.register<ConfigureShadowRelocation>("relocateShadowJar") {
    target = tasks.shadowJar.get()
    prefix = project.property("props.root-package").toString() + ".shadow"
}

tasks.shadowJar {
    // Makes sure the name of the final jar is (plugin-display-name).jar
    archiveFileName.set(metadata.displayName + ".jar")
    // Set the classifier to plugin for publication on a maven repository
    archiveClassifier.set("plugin")
    // Configure the dependencies shading
    dependsOn(relocate)
    // Reduce shadow jar size by removing unused classes.
    // Warning, if one of your dependencies use service loaders or reflection, add to the exclude list
    // such as "minimize { exclude(dependency("some.group:some-dependency:.*")) }"
    minimize()
    // Include the plugin.json file with the modified version
    doFirst {
        val temp = temporaryDir.resolve("plugin.json")
        temp.writeText(metadata.toJson(true))
        from(temp)
    }
    // Include the license of your project
    from(rootProject.file("LICENSE.md")) {
        into("META-INF")
    }
}

tasks.build {
    // Make sure the shadow jar is built during the build task
    dependsOn(tasks.shadowJar)
}

tasks.runMindustryServer {
    mods.setFrom(tasks.shadowJar, mindustryRuntime)
}

tasks.runMindustryClient {
    mods.setFrom()
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
}

indra {
    javaVersions {
        target(17)
        minimumToolchain(17)
    }

    publishSnapshotsTo("xpdustry", "https://maven.xpdustry.fr/snapshots")
    publishReleasesTo("xpdustry", "https://maven.xpdustry.fr/releases")

    // The license of your project, kyori has already functions for the most common licenses
    // such as gpl3OnlyLicense() for GPLv3, apache2License() for Apache 2.0, etc.
    // You can still specify your own license using the license { } builder function.
    mitLicense()

    if (metadata.repo.isNotBlank()) {
        val repo = metadata.repo.split("/")
        github(repo[0], repo[1]) {
            ci(true)
            issues(true)
            scm(true)
        }
    }

    configurePublications {
        pom {
            organization {
                name.set("Xpdustry")
                url.set("https://www.xpdustry.fr")
            }

            developers {
                developer {
                    id.set(metadata.author)
                }
            }
        }
    }
}

spotless {
    java {
        palantirJavaFormat()
        formatAnnotations()
        importOrderFile(rootProject.file(".spotless/project.importorder"))
        custom("noWildcardImports") {
            if (it.contains("*;\n")) {
                throw Error("No wildcard imports allowed")
            }
            it
        }
        bumpThisNumberIfACustomStepChanges(1)
    }
    kotlinGradle {
        ktlint()
    }
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("LICENSE_HEADER.md"))
    // Some properties to make updating the licence header easier
    property("NAME", metadata.displayName)
    property("DESCRIPTION", metadata.description)
    property("AUTHOR", metadata.author)
    property("YEAR", property("props.project-year").toString())
}
