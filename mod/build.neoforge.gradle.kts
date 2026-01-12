plugins {
    id("net.neoforged.moddev") version "2.0.137"
    id("maven-publish")
    id("signing")
    id("com.gradleup.nmcp")
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
        accessTransformers.from(rootProject.file("mod/src/main/resources/META-INF/accesstransformer.cfg"))
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
    withJavadocJar()
}

tasks.withType<Javadoc>().configureEach {
    isFailOnError = false
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

// Publishing Configuration

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