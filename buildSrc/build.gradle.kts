import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.github.schaka.rarrnomore"

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "18"
    }
}