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
include("services:booking-service")
include("api-gateway")
include("service-registry")

plugins {
    id("org.gradle.toolchains.foojay-resolver") version "0.8.0" // Use the latest version
}