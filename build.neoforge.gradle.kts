plugins {
    id("net.neoforged.moddev") version "2.0.137"
    id("maven-publish")
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

neoForge {
    version = property("deps.neoforge").toString()
    if (stonecutter.eval(stonecutter.current.version, ">=1.21.10")) {
        accessTransformers.from(rootProject.file("src/main/resources/META-INF/accesstransformer.cfg"))
    }

    val runDir = "../../run"
    runs {
        register("client") {
            client()
            gameDirectory = file(runDir)
            ideName = "NeoForge Client (${stonecutter.active?.version})"
            programArgument("--username=ClickToPlay")
        }
        register("server") {
            server()
            gameDirectory = file(runDir)
            ideName = "NeoForge Server (${stonecutter.active?.version})"
        }
    }
    mods {
        register(property("mod.id").toString()) {
            sourceSet(sourceSets["main"])
        }
    }
}

tasks.named("createMinecraftArtifacts") {
    // This tells NeoForge to wait until Stonecutter has generated the files
    mustRunAfter(tasks.named("stonecutterGenerate"))
}

java {
    withSourcesJar()
}

tasks.processResources {
    val properties = mapOf(
        "mod_version" to modVersion,
        "minecraft_version_range" to project.property("mod.minecraft_version_range"),
        "minecraft_version" to project.property("mod.minecraft_version"),
    )
    filesMatching(listOf("META-INF/neoforge.mods.toml", "META-INF/mods.toml")) {
        expand(properties)
    }
    inputs.properties(properties)
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
            from(components["java"])
            groupId = group.toString()
            artifactId = archivesBaseName
            version = fullVersion
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