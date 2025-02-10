plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
    id("org.jetbrains.intellij") version "1.17.4"
    id("com.github.node-gradle.node") version "7.1.0"
}

group = "dev.camunda.bpmn"
version = "1.4.3"

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.2.6")
    type.set("IC")
    plugins.set(listOf())
}

node {
    version.set("23.1.0")
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
}