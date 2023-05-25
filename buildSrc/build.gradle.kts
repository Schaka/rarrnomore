import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.github.schaka.rarrnomore"

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_18.toString()
    targetCompatibility = JavaVersion.VERSION_18.toString()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JavaVersion.VERSION_18.toString()
    }
}