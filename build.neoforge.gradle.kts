plugins {
    id("net.neoforged.moddev") version "2.0.137"
    id("maven-publish")
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

dependencies {

}

neoForge {
    version = property("deps.neoforge").toString()
    accessTransformers.from(rootProject.file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        register("client") {
            client()
            gameDirectory = file("run/")
            ideName = "NeoForge Client (${stonecutter.active?.version})"
            programArgument("--username=ClickToPlay")
        }
        register("server") {
            server()
            gameDirectory = file("run/")
            ideName = "NeoForge Server (${stonecutter.active?.version})"
        }
    }
    mods {
        register(property("mod.id").toString()) {
            sourceSet(sourceSets["main"])
        }
    }
}

base {
    archivesName.set(property("archives_base_name").toString())
}

tasks.named("createMinecraftArtifacts") {
    // This tells NeoForge to wait until Stonecutter has generated the files
    mustRunAfter(tasks.named("stonecutterGenerate"))
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

val env = System.getenv()

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = group.toString()
            artifactId = "sgui"
            version = version.toString()
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