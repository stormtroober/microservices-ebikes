plugins {
    java
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vertx:vertx-core:4.4.0")
    implementation("io.vertx:vertx-web:4.4.0")
}

application {
    mainClass.set("org.example.MainVerticle")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get() // or specify your main class directly
    }

    // Include all runtime dependencies into the JAR file
    from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) })

    // Optionally, include your compiled classes (if not already included by default)
    from(sourceSets.main.get().output)

    // Ensure the JAR is built as a single fat JAR
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


tasks.named<JavaExec>("run") {
    doFirst {
        println("Starting the Vert.x application...")
    }
}