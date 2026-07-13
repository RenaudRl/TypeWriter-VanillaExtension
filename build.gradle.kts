plugins {
    kotlin("jvm") version "2.3.20"
    id("com.typewritermc.module-plugin") version "2.1.0"
}

group = "btcrenaud"
version = "0.1.2"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://maven.typewritermc.com/beta/")
    maven("https://maven.typewritermc.com/external/")
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.typewritermc:BasicExtension:0.9.0")
    implementation("com.typewritermc:QuestExtension:0.9.0")
    implementation("com.typewritermc:EntityExtension:0.9.0")
    implementation("com.typewritermc:RoadNetworkExtension:0.9.0")
}

typewriter {
    namespace = "renaud"

    extension {
        name = "Vanilla"
        shortDescription = "Typewriter extension for Differents Vanilla entries support."
        description = """Typewriter extension module providing additional entries for the Typewriter plugin ecosystem. Supports Paper and Folia server platforms with full feature parity. This module extends the core functionality with specialized entries. Compatible with the official Typewriter engine and designed for standalone use."""
        engineVersion = "0.9.0-beta-175"
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA
        
        paper()

        dependencies {
            dependency("typewritermc", "Basic")
            dependency("typewritermc", "Quest")
            dependency("typewritermc", "Entity")
            dependency("typewritermc", "RoadNetwork")
        }
    }
}

    

kotlin {
    jvmToolchain(21)
}

