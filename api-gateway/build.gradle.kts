plugins {
	java
	id("org.springframework.boot") 
	id("io.spring.dependency-management")
}

group = "com.booking"
version = "0.0.1-SNAPSHOT"
description = "Bus Booking System API Gateway"

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
	
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}