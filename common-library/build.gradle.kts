plugins {
    `java-library`
	id("org.springframework.boot")
}
group = "com.booking"
version = "0.0.1-SNAPSHOT"
description = "Bus Booking System Common Library"

repositories {
	mavenCentral()
}

dependencies {
	implementation("jakarta.validation:jakarta.validation-api:3.1.1")
	implementation("org.springframework.boot:spring-boot-starter")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}