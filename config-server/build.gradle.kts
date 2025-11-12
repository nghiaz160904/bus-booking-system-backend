plugins {
	java
	id("org.springframework.boot")
	id("io.spring.dependency-management")
}

group = "com.booking"
version = "0.0.1-SNAPSHOT"
description = "Bus Booking System Config Server"

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.cloud:spring-cloud-config-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}