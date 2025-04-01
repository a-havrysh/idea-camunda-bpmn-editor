plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20-RC"
    id("org.jetbrains.intellij.platform") version "2.5.0"
    id("com.github.node-gradle.node") version "7.1.0"
}

group = "dev.camunda.bpmn"
version = "1.4.4"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

node {
    version.set("23.10.0")
    download.set(true)
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("251.*")
    }

    val buildNpm = register<com.github.gradle.node.npm.task.NpmTask>("buildNpm") {
        args.set(listOf("run", "build"))
        workingDir.set(file("${project.projectDir}"))
    }

    buildPlugin {
        dependsOn(buildNpm)
    }
}

sourceSets {
    main {
        resources {
            srcDir("${project.projectDir}/build/public")
        }
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    intellijPlatform {
        intellijIdeaCommunity("2024.3.5")
    }
}