import java.util.Properties

val props = Properties()
file("./gradle.properties").reader().use { props.load(it) }
rootProject.name = props.getProperty("props.project-name")
