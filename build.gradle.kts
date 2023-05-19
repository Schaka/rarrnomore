import com.google.cloud.tools.jib.api.buildplan.ImageFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maven-publish")

    id("org.springframework.boot") version "3.0.6"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.graalvm.buildtools.native") version "0.9.20"
    id("com.google.cloud.tools.jib") version "3.3.2"

    kotlin("jvm") version "1.8.20"
    kotlin("plugin.spring") version "1.8.20"

}

group = "com.github.schaka."
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_18

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "18"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jib {
    to {
        image = "docker.io/schaka/rarrnomore"
        auth {
            username = System.getenv("DOCKERHUB_USER")
            password = System.getenv("DOCKERHUB_PASSWORD")
        }
    }
    from {
        image = "eclipse-temurin:18-jre-jammy"
        auth {
            username = System.getenv("DOCKERHUB_USER")
            password = System.getenv("DOCKERHUB_PASSWORD")
        }
    }
    container {
        jvmFlags = listOf("-Dspring.config.additional-location=optional:file:/config/application.yaml", "-Xms512m")
        mainClass = "com.github.schaka.rarrnomore.RarrnomoreApplicationKt"
        ports = listOf("8978")
        format = ImageFormat.Docker
        volumes = listOf("/config")
    }
}