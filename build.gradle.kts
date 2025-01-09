val fabricKotlinVersion: String by project
val javaVersion = JavaVersion.VERSION_21
val loaderVersion: String by project
val minecraftVersion: String by project
val modVersion: String by project
val mavenGroup: String by project
val modId: String by project

plugins {
    id("fabric-loom")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

base {
    archivesName.set("$modId-$modVersion-$minecraftVersion")
}

loom {
    accessWidenerPath = file("src/main/resources/${modId}.accesswidener")
}

repositories {
    maven("https://api.modrinth.com/maven") {
        content {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    minecraft("com.mojang", "minecraft", minecraftVersion)

    val yarnMappings: String by project
    mappings("net.fabricmc", "yarn", yarnMappings, null, "v2")

    modImplementation("net.fabricmc", "fabric-loader", loaderVersion)

    val fabricVersion: String by project
    modImplementation("net.fabricmc.fabric-api", "fabric-api", fabricVersion)


    modImplementation("net.fabricmc", "fabric-language-kotlin", fabricKotlinVersion)

    val modMenuBadgesLibVersion: String by project
    include(modImplementation("maven.modrinth", "modmenu-badges-lib", modMenuBadgesLibVersion))

    val duckyUpdaterLibVersion: String by project
    include(modImplementation("maven.modrinth", "ducky-updater-lib", duckyUpdaterLibVersion))

    include(implementation("org.apache.xmlgraphics", "batik-transcoder", "1.16") {
        exclude("xml-apis", "xml-apis")
    })
    include(implementation("org.apache.xmlgraphics", "batik-codec", "1.16") {
        exclude("xml-apis", "xml-apis")
    })
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
        }
    }

    jar {
        from("LICENSE")
    }

    processResources {
        val modName: String by project
        val modDescription: String by project

        filesMatching("fabric.mod.json") {
            expand(
                mutableMapOf(
                    "modId" to modId,
                    "modName" to modName,
                    "modVersion" to modVersion,
                    "minecraftVersion" to minecraftVersion,
                    "javaVersion" to javaVersion.toString()
                )
            )
        }
        filesMatching("template.mixins.json") {
            expand(mutableMapOf("modId" to modId))
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
        }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }
}
