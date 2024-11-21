plugins {
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    java
}

group = "com.eurekagateway.application"
version = "1.0.0-SNAPSHOT"
description = "Eureka Gateway Application"

dependencies {
    // Spring Cloud Gateway Starter
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    // Spring Cloud Eureka Discovery Client Starter
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // Spring Boot Starter
    implementation("org.springframework.boot:spring-boot-starter")
}

extra["springCloudVersion"] = "2023.0.3"

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}


repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}