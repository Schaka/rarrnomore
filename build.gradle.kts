import com.google.cloud.tools.jib.api.buildplan.ImageFormat
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import net.nemerosa.versioning.VersioningExtension
import org.springframework.boot.gradle.dsl.SpringBootExtension

plugins {

    id("idea")
    id("org.springframework.boot") version "3.0.6"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.graalvm.buildtools.native") version "0.9.20"
    id("com.google.cloud.tools.jib") version "3.3.2"
    id("net.nemerosa.versioning") version "2.8.2"

    kotlin("jvm") version "1.8.21"
    kotlin("plugin.spring") version "1.8.21"

}

repositories {
    gradlePluginPortal()
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

configure<SpringBootExtension> {
    buildInfo()
}

configure<IdeaModel> {
    module {
        inheritOutputDirs = true
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(18))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
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

configure<VersioningExtension> {
    /**
     * Add GitHub CI branch name environment variable
     */
    branchEnv = listOf("GITHUB_REF_NAME")
}

extra {
    val build = getBuild()
    val versioning: VersioningExtension = extensions.getByName<VersioningExtension>("versioning")
    val branch = versioning.info.branch
    val shortCommit = versioning.info.commit.take(8)

    project.extra["build.date-time"] = build.buildDateAndTime
    project.extra["build.date"] = build.formattedBuildDate()
    project.extra["build.time"] = build.formattedBuildTime()
    project.extra["build.revision"] = versioning.info.commit
    project.extra["build.revision.abbreviated"] = shortCommit
    project.extra["build.branch"] = branch
    project.extra["build.user"] = build.userName()

    val containerImageName = "schaka/${project.name}"
    val containerImageTagVersion = if (branch == "main") shortCommit else branch
    val containerImageTags = if (branch == "main") setOf(containerImageTagVersion, "latest") else setOf(containerImageTagVersion)

    project.extra["docker.image.name"] = containerImageName
    project.extra["docker.image.version"] = containerImageTagVersion
    project.extra["docker.image.source"] = build.projectSourceRoot()
    project.extra["docker.image.tags"] = containerImageTags

}

jib {
    to {
        image = "docker.io/${project.extra["docker.image.name"]}"
        tags = project.extra["docker.image.tags"] as Set<String>

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

        labels.set(
            mapOf(
                "org.opencontainers.image.created" to "${project.extra["build.date"]}T${project.extra["build.time"]}",
                "org.opencontainers.image.revision" to project.extra["build.revision"] as String,
                "org.opencontainers.image.version" to project.version as String,
                "org.opencontainers.image.title" to project.name,
                "org.opencontainers.image.authors" to "Schaka <schaka@github.com>",
                "org.opencontainers.image.source" to project.extra["docker.image.source"] as String,
                "org.opencontainers.image.description" to project.description,
            )
        )


        // Exclude all "developmentOnly" dependencies, e.g. Spring devtools.
        configurationName.set("productionRuntimeClasspath")
    }
}