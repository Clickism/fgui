plugins {
	id("net.fabricmc.fabric-loom-remap") version "1.14-SNAPSHOT"
	id("maven-publish")
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
	// Loom automatically attaches sourcesJar to RemapSourcesJar and "build"
	withSourcesJar()
}

val archivesBaseName = project.property("archives_base_name").toString()

val modVersion = project.property("mod.version").toString()
val minecraftVersion = stonecutter.current.project.substringBeforeLast('-')
val loader = stonecutter.current.project.substringAfterLast('-')

version = "$modVersion+$minecraftVersion-$loader"
group = project.property("maven_group").toString()

repositories {
	maven("https://jitpack.io")
}

base {
	archivesName.set(archivesBaseName)
}

loom {
	accessWidenerPath.set(rootProject.file("src/main/resources/sgui.accesswidener"))
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

val env = System.getenv()

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifact(tasks.remapJar) {
				builtBy(tasks.remapJar)
			}
			artifact(tasks.remapSourcesJar) {
				builtBy(tasks.remapSourcesJar)
			}
		}
	}

	repositories {
		if (env["MAVEN_URL"] != null) {
			maven {
				url = uri(env["MAVEN_URL"]!!)
				credentials {
					username = env["MAVEN_USERNAME"]
					password = env["MAVEN_PASSWORD"]
				}
			}
		} else {
			mavenLocal()
		}
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
	}
}

dependencies {
	// Test Mod Dependencies
	"fabricTestModImplementation"(sourceSets.main.get().output)
}