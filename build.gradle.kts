plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.16.0"
}

group = "com.example"
version = "2.2.7"

repositories {
    mavenCentral()
    maven("https://plugins.jetbrains.com/maven")
    maven("https://maven.aliyun.com/repository/central")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2.5")
    type.set("IU") // Target IDE Platform

    plugins.set(listOf("com.intellij.java"))
}
sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("235.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
