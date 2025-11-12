import org.gradle.api.plugins.JavaPluginExtension
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.springframework.boot.gradle.tasks.bundling.BootJar

val springCloudVersion = "2025.0.0"

extra.set("springCloudVersion", springCloudVersion)

plugins {
    id("org.springframework.boot") version "3.5.7" apply false 

    id("io.spring.dependency-management") version "1.1.7" apply false
    
}

group = "com.booking"
version = "0.0.1-SNAPSHOT"
description = "Bus Ticket booking platform"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    // Apply plugins
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    group = "com.booking"
    version = "0.0.1-SNAPSHOT"

    configure<JavaPluginExtension> { 
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }
    
    configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}")
        }
    }
}

project(":services") {
    tasks.named<BootJar>("bootJar").configure {
        enabled = false
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
// docker swarm init docker stack deploy -c docker-compose.yml bus-booking-stack