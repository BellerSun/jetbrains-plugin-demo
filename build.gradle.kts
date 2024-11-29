plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.16.0"
}

group = "com.example"
version = "2.2.10"

repositories {
    mavenCentral()
    maven("https://plugins.jetbrains.com/maven")
    maven("https://maven.aliyun.com/repository/central")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    // 注释掉版本
    // version.set("2024.1.1")
   localPath.set("/Applications/CLion 2024.1.6.app/Contents") // 设置本地 IDE 的路径
    //localPath.set("/Applications/CLion 2020.3.app/Contents") // 设置本地 IDE 的路径
    // localPath.set("/Applications/CLion 2019.1.4.app/Contents") // 设置本地 IDE 的路径
    //type.set("CL") // 根据本地 IDE 类型设置，例如 PyCharm Community 是 "IC"（IntelliJ Community）

   plugins.set(listOf("terminal","com.intellij.clion","com.intellij.cidr.lang"))
    // plugins.set(listOf("terminal","com.intellij.cidr.lang"))
}
sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
    }
}

val pycharmPath by extra("/Applications/CLion 2024.1.6.app")
//val pycharmPath by extra("/Applications/CLion 2019.1.4.app")

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(fileTree(mapOf("dir" to "$pycharmPath/Contents/lib", "include" to listOf("**/*.jar"))))

    // https://mvnrepository.com/artifact/cn.hutool/hutool-all
    implementation("cn.hutool:hutool-all:5.8.25")
// lombok
    implementation("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    // https://mvnrepository.com/artifact/io.github.java-diff-utils/java-diff-utils
    implementation("io.github.java-diff-utils:java-diff-utils:4.12")
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
        sinceBuild.set("183")
        untilBuild.set("242.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
    buildSearchableOptions{
        enabled = false
    }
}
