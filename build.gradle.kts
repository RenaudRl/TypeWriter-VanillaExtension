

plugins {
    kotlin("jvm") version "2.0.21"
    id("com.typewritermc.module-plugin") version "1.3.0"
}

group = "btc.renaud.vanillaextension"
version = "0.9.0" // The version is the same with the plugin to avoid confusion. :)

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("com.typewritermc:QuestExtension:0.9.0")
    implementation("com.typewritermc:BasicExtension:0.9.0")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

typewriter {
    namespace = "renaud"

    extension {
        name = "Vanilla"
        shortDescription = "Typewriter extension for Differents Vanilla entries support."
        description =
            "This extension adds support for various vanilla Minecraft features in Typewriter, allowing you to configure triggers for enchantment application in both enchanting tables and anvils."
        engineVersion = "0.9.0-beta-162"
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA
        dependencies {
            dependency("typewritermc", "Quest")
        }
        paper()

    }

}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
