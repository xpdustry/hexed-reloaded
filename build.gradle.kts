import fr.xpdustry.toxopid.dsl.mindustryDependencies
import fr.xpdustry.toxopid.spec.ModMetadata
import fr.xpdustry.toxopid.spec.ModPlatform
import fr.xpdustry.toxopid.task.GithubArtifactDownload
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("com.diffplug.spotless") version "6.23.3"
    id("net.kyori.indra") version "3.1.3"
    id("net.kyori.indra.publishing") version "3.1.3"
    id("net.kyori.indra.git") version "3.1.3"
    id("net.kyori.indra.licenser.spotless") version "3.1.3"
    id("net.ltgt.errorprone") version "3.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("fr.xpdustry.toxopid") version "3.2.0"
    id("com.github.ben-manes.versions") version "0.50.0"
}

val metadata = ModMetadata.fromJson(file("plugin.json"))

// Remove the following line if you don't want snapshot versions
if (indraGit.headTag() == null) {
    metadata.version += "-SNAPSHOT"
}

group = "com.xpdustry"
val rootPackage = "com.xpdustry.template"
version = metadata.version
description = metadata.description

toxopid {
    compileVersion.set("v${metadata.minGameVersion}")
    platforms.set(setOf(ModPlatform.HEADLESS))
}

repositories {
    mavenCentral()
    // This repository provides mindustry artifacts built by xpdustry
    maven("https://maven.xpdustry.com/mindustry")
    // This repository provides xpdustry libraries, such as the distributor-api
    maven("https://maven.xpdustry.com/releases")
}

dependencies {
    mindustryDependencies()
    compileOnly("fr.xpdustry:distributor-api:3.3.0")

    val junit = "5.10.1"
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit")

    val checker = "3.41.0"
    compileOnly("org.checkerframework:checker-qual:$checker")
    testImplementation("org.checkerframework:checker-qual:$checker")

    // Static analysis
    annotationProcessor("com.uber.nullaway:nullaway:0.12.1")
    errorprone("com.google.errorprone:error_prone_core:2.23.0")
}

indra {
    javaVersions {
        target(17)
        minimumToolchain(17)
    }

    publishSnapshotsTo("xpdustry", "https://maven.xpdustry.com/snapshots")
    publishReleasesTo("xpdustry", "https://maven.xpdustry.com/releases")

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
                name.set("xpdustry")
                url.set("https://www.xpdustry.com")
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
}

spotless {
    java {
        // Palantir is an excellent java linter, balanced between google codestyle and regular java codestyle
        palantirJavaFormat()
        formatAnnotations()
        // Makes sure static imports are after normal imports
        importOrderFile(rootProject.file(".spotless/project.importorder"))
        // Makes sure there is no wildcard imports.
        // If you want to allow them,
        // remove the following line and the bumpThisNumberIfACustomStepChanges(1)
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
    property("YEAR", "2023")
}

tasks.withType<JavaCompile> {
    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
        disable("MissingSummary", "InlineMeSuggester", "FutureReturnValueIgnored")
        if (!name.contains("test", ignoreCase = true)) {
            check("NullAway", CheckSeverity.ERROR)
            option("NullAway:AnnotatedPackages", rootPackage)
            option("NullAway:TreatGeneratedAsUnannotated", true)
        }
    }
}

// Required for the GitHub actions
tasks.register("getArtifactPath") {
    doLast { println(tasks.shadowJar.get().archiveFile.get().toString()) }
}

tasks.shadowJar {
    // Makes sure the name of the final jar is (plugin-name).jar
    archiveFileName.set("${metadata.name}.jar")
    // Set the classifier to plugin for publication on a maven repository
    archiveClassifier.set("plugin")
    // Configure the dependencies shading.
    // WARNING: SQL drivers do not play well with shading,
    // the best solution would be to load them in an isolated classloader.
    // If it's too difficult, you can disable relocation but be aware this can conflict with other plugins.
    isEnableRelocation = true
    relocationPrefix = "$rootPackage.shadow"
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

tasks.register<Copy>("release") {
    dependsOn(tasks.build)
    from(tasks.shadowJar)
    destinationDir = temporaryDir
}

val downloadDistributorCore =
    tasks.register<GithubArtifactDownload>("downloadDistributorCore") {
        user.set("xpdustry")
        repo.set("distributor")
        version.set("v3.3.0")
        name.set("distributor-core.jar")
    }

tasks.runMindustryServer {
    mods.setFrom(tasks.shadowJar, downloadDistributorCore)
}
