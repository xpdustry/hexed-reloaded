import com.xpdustry.toxopid.Toxopid
import com.xpdustry.toxopid.extension.anukeXpdustry
import com.xpdustry.toxopid.extension.configureDesktop
import com.xpdustry.toxopid.spec.ModMetadata
import com.xpdustry.toxopid.spec.ModPlatform
import com.xpdustry.toxopid.task.GithubAssetDownload
import com.xpdustry.toxopid.task.MindustryExec
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("com.diffplug.spotless") version "8.8.0"
    id("net.kyori.indra") version "4.0.0"
    id("net.kyori.indra.git") version "4.0.0"
    id("net.kyori.indra.publishing") version "4.0.0"
    id("com.gradleup.shadow") version "9.4.3"
    id("com.xpdustry.toxopid") version "4.2.0"
    id("net.ltgt.errorprone") version "5.1.0"
}

val metadata = ModMetadata.fromJson(rootProject.file("plugin.json"))
if (!findProperty("is_release").toString().toBoolean()) metadata.version += "-SNAPSHOT"
group = "com.xpdustry"
version = metadata.version
description = metadata.description

toxopid {
    compileVersion = "v${metadata.minGameVersion}"
    platforms = setOf(ModPlatform.SERVER)
}

repositories {
    mavenCentral()
    anukeXpdustry()
    maven("https://maven.xpdustry.com/releases") {
        name = "xpdustry-releases"
        mavenContent { releasesOnly() }
    }
}

dependencies {
    compileOnly(toxopid.dependencies.mindustryCore)
    compileOnly(toxopid.dependencies.arcCore)

    implementation("com.xpdustry:distributor-command-cloud:4.2.0")
    implementation("org.incendo:cloud-core:2.0.0")
    implementation("org.incendo:cloud-annotations:2.0.0")
    compileOnly("com.xpdustry:distributor-common-api:4.2.0")
    compileOnly("org.jspecify:jspecify:1.0.0")
    annotationProcessor("com.uber.nullaway:nullaway:0.13.7")
    errorprone("com.google.errorprone:error_prone_core:2.50.0")
}

indra {
    javaVersions {
        target(25)
        minimumToolchain(25)
    }

    publishSnapshotsTo("xpdustry", "https://maven.xpdustry.com/snapshots")
    publishReleasesTo("xpdustry", "https://maven.xpdustry.com/releases")

    gpl3OnlyLicense()

    if (metadata.repository.isNotBlank()) {
        val repo = metadata.repository.split("/")
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
    useInMemoryPgpKeys(
        project.findProperty("signingKey")?.toString(),
        project.findProperty("signingPassword")?.toString(),
    )
}

spotless {
    java {
        palantirJavaFormat()
        formatAnnotations()
        importOrder("", "\\#")
        forbidWildcardImports()
        forbidModuleImports()
        licenseHeader("// SPDX-License-Identifier: GPL-3.0-only")
    }
    kotlinGradle {
        ktlint().editorConfigOverride(mapOf("max_line_length" to "120"))
    }
}

val generateMetadataFile = tasks.register("generateMetadataFile") {
    inputs.property("metadata", metadata)
    val output = temporaryDir.resolve("plugin.json")
    outputs.file(output)
    doLast { output.writeText(ModMetadata.toJson(metadata)) }
}

tasks.shadowJar {
    archiveFileName = "${metadata.name}.jar"
    archiveClassifier = "plugin"
    from(generateMetadataFile)
    from(rootProject.file("LICENSE.md")) { into("META-INF") }
    relocate("com.xpdustry.distributor.api.command.cloud", "com.xpdustry.hexed.shadow.cloud")
    relocate("org.incendo.cloud", "com.xpdustry.hexed.shadow.cloud")
    relocate("io.leangen.geantyref", "com.xpdustry.hexed.shadow.geantyref")
    minimize()
    mergeServiceFiles()
}

tasks.register<Copy>("release") {
    dependsOn(tasks.build)
    from(tasks.shadowJar)
    destinationDir = temporaryDir
}

tasks.withType<JavaCompile> {
    options.errorprone {
        disableWarningsInGeneratedCode = true
        disable("MissingSummary", "InlineMeSuggester")
        if (!name.contains("test", ignoreCase = true)) {
            check("NullAway", CheckSeverity.ERROR)
            option("NullAway:OnlyNullMarked")
            option("NullAway:JSpecifyMode", "true")
        }
    }
}

val downloadSlf4md = tasks.register<GithubAssetDownload>("downloadSlf4md") {
    owner = "xpdustry"
    repo = "slf4md"
    asset = "slf4md-simple.jar"
    version = "v1.0.4"
}

val downloadDistributorCommon = tasks.register<GithubAssetDownload>("downloadDistributorCommon") {
    owner = "xpdustry"
    repo = "distributor"
    asset = "distributor-common.jar"
    version = "v4.2.0"
}

tasks.runMindustryServer {
    mods.from(downloadSlf4md, downloadDistributorCommon)
}

tasks.register<MindustryExec>("runMindustryDesktop2") {
    group = Toxopid.TASK_GROUP_NAME
    configureDesktop()
    classpath(tasks.downloadMindustryDesktop)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
