plugins {
	id("net.fabricmc.fabric-loom-remap") version "1.14-SNAPSHOT"
	id("maven-publish")
	id("signing")
	id("com.gradleup.nmcp") version "1.4.3"
	id("com.gradleup.nmcp.aggregation") version "1.4.3"
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
	// Loom automatically attaches sourcesJar to RemapSourcesJar and "build"
	withSourcesJar()
	withJavadocJar()
}

tasks.withType<Javadoc>().configureEach {
	isFailOnError = false
}

val modVersion = project.property("mod.version").toString()
val minecraftVersion = stonecutter.current.project.substringBeforeLast('-')
val loader = stonecutter.current.project.substringAfterLast('-')

val archivesBaseName = "${project.property("archives_base_name")}-$loader"
val fullVersion = "$modVersion+$minecraftVersion"

version = fullVersion
group = project.property("maven_group").toString()

repositories {
	mavenCentral()
}

base {
	archivesName.set(archivesBaseName)
}

loom {
	if (stonecutter.eval(stonecutter.current.version, ">=1.21.10")) {
		accessWidenerPath.set(rootProject.file("src/main/resources/fgui.accesswidener"))
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${project.property("mod.minecraft_version")}")
	mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:${project.property("deps.fabric_loader")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("deps.fabric_api")}")
}

tasks.processResources {
	val props = mapOf(
		"mod_version" to version,
		"minecraft_version" to project.property("mod.minecraft_version"),
		"minecraft_version_range" to project.property("mod.minecraft_version_range"),
		"fabric_loader_version" to project.property("deps.fabric_loader")
	)
	filesMatching("fabric.mod.json") {
		expand(props)
	}
	inputs.properties(props)
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.release.set(21)
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${archivesBaseName}" }
	}
}

stonecutter {
	replacements {
		string(current.parsed < "1.21.11") {
			replace("Identifier", "ResourceLocation")
		}
	}
}

// Publishing Setup
dependencies {
	// Use allprojects as ncmp-settings plugin does not work with stonecutter?
	allprojects {
		nmcpAggregation(project(path))
	}
}

nmcpAggregation {
	centralPortal {
		username = providers.gradleProperty("ossrhUsername").orNull
		password = providers.gradleProperty("ossrhPassword").orNull
		publishingType = "USER_MANAGED"
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			groupId = group.toString()
			artifactId = archivesBaseName
			version = fullVersion
			pom {
				name.set("fgui")
				description.set("Library for creating custom, server side guis on Fabric/Neoforge")
				url.set("https://github.com/Clickism/fgui")
				licenses {
					license {
						name.set("GNU General Public License v3.0")
						url.set("https://www.gnu.org/licenses/gpl-3.0.html")
					}
				}
				developers {
					developer {
						id.set("Clickism")
						name.set("Clickism")
						email.set("dev@clickism.de")
					}
				}
				scm {
					connection.set("scm:git:git://github.com/Clickism/fgui.git")
					developerConnection.set("scm:git:ssh://github.com/Clickism/fgui.git")
					url.set("https://github.com/Clickism/fgui")
				}
			}
		}
	}
	signing {
		sign(publishing.publications["mavenJava"])
	}
}

// Configure Test Mod
sourceSets {
	create("fabricTestMod") {
		runtimeClasspath += main.get().runtimeClasspath
		compileClasspath += main.get().compileClasspath
	}
}

loom {
	runs {
		create("fabricTestModClient") {
			client()
			ideConfigGenerated(project.rootProject == project)
			name("Fabric Test Mod Client")
			source(sourceSets.getByName("fabricTestMod"))
		}
		create("fabricTestModServer") {
			server()
			ideConfigGenerated(project.rootProject == project)
			name("Fabric Test Mod Server")
			source(sourceSets.getByName("fabricTestMod"))
		}
		configureEach {
			runDir = "../../run"
			ideConfigGenerated(true)
		}
	}
}

dependencies {
	// Test Mod Dependencies
	"fabricTestModImplementation"(sourceSets.main.get().output)
}