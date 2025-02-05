import com.xpdustry.toxopid.extension.anukeXpdustry
import com.xpdustry.toxopid.spec.ModMetadata
import com.xpdustry.toxopid.spec.ModPlatform
import com.xpdustry.toxopid.task.GithubAssetDownload
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    alias(libs.plugins.spotless)
    alias(libs.plugins.indra.common)
    alias(libs.plugins.indra.git)
    alias(libs.plugins.indra.publishing)
    alias(libs.plugins.shadow)
    alias(libs.plugins.toxopid)
    alias(libs.plugins.errorprone.gradle)
}

val metadata = ModMetadata.fromJson(rootProject.file("plugin.json"))
if (indraGit.headTag() == null) metadata.version += "-SNAPSHOT"
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
    implementation(libs.distributor.cloud)
    implementation(libs.cloud.core)
    implementation(libs.cloud.annotations)
    compileOnly(libs.distributor.api)
    compileOnly(libs.checker.qual)
    annotationProcessor(libs.nullaway)
    errorprone(libs.errorprone.core)
}

indra {
    javaVersions {
        target(libs.versions.java.get().toInt())
        minimumToolchain(libs.versions.java.get().toInt())
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
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
}

spotless {
    java {
        palantirJavaFormat()
        formatAnnotations()
        importOrder("", "\\#")
        // TODO: SPOTLESS, WHY
        // custom("no-wildcard-imports") { it.apply { if (contains("*;\n")) error("No wildcard imports allowed") } }
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        bumpThisNumberIfACustomStepChanges(1)
    }
    kotlinGradle {
        ktlint().editorConfigOverride(mapOf("max_line_length" to "120"))
    }
}

val generateMetadataFile by tasks.registering {
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
            option("NullAway:AnnotatedPackages", "com.xpdustry.hexed")
            option("NullAway:TreatGeneratedAsUnannotated", true)
        }
    }
}

val downloadSlf4md by tasks.registering(GithubAssetDownload::class) {
    owner = "xpdustry"
    repo = "slf4md"
    asset = "slf4md-simple.jar"
    version = "v${libs.versions.slf4md.get()}"
}

val downloadDistributorCommon by tasks.registering(GithubAssetDownload::class) {
    owner = "xpdustry"
    repo = "distributor"
    asset = "distributor-common.jar"
    version = "v${libs.versions.distributor.get()}"
}

tasks.runMindustryServer {
    mods.from(downloadSlf4md, downloadDistributorCommon)
}
