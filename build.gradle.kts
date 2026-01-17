plugins {
    kotlin("jvm") version "2.2.10"
    id("com.typewritermc.module-plugin")
}

group = "btc.renaud.vanillaextension"
version = "0.0.4"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
    maven("https://maven.typewritermc.com/beta/")
    maven("https://jitpack.io/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.infernalsuite.com/repository/maven-snapshots/")
}

dependencies {
    implementation("com.typewritermc:QuestExtension:0.9.0")
    implementation("com.typewritermc:BasicExtension:0.9.0")
    implementation(project(":EntityExtension"))
    implementation(project(":RoadNetworkExtension"))
    implementation(project(":MythicMobsExtension"))
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.infernalsuite.asp:api:4.0.0-SNAPSHOT")
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
}

typewriter {
    namespace = "renaud"
    extension {
        name = "Vanilla"
        shortDescription = "Typewriter extension for Differents Vanilla entries support."
        description = "This extension adds support for various vanilla Minecraft features in Typewriter, allowing you to configure triggers for enchantment application in both enchanting tables and anvils."
        engineVersion = file("../../version.txt").readText().trim()
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA
        dependencies { dependency("typewritermc", "Quest") }
        paper()
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21) }
}

