pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "bus-booking-system-backend"

include("common-library")
include("config-server")
include("services:user-service")
include("api-gateway")

plugins {
    id("org.gradle.toolchains.foojay-resolver") version "0.8.0" // Use the latest version
}