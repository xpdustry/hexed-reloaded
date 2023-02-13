import java.util.Properties

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.xpdustry.fr/snapshots")
    }
}

val props = Properties()
file("./gradle.properties").reader().use { props.load(it) }
rootProject.name = props.getProperty("props.project-name")
