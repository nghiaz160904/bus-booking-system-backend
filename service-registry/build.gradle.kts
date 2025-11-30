plugins {
	java
	id("org.springframework.boot") 
	id("io.spring.dependency-management")
}

group = "com.booking"
version = "0.0.1-SNAPSHOT"
description = "Bus Booking System Service Registry"

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.retry:spring-retry")
    // Spring Cloud Dependencies
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.named<Test>("test") {
    enabled = false
}