import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    application
}

group = "id.walt.webwallet"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.walt.id/repository/waltid/")
    maven("https://maven.walt.id/repository/waltid-ssi-kit/")

    maven("https://maven.walt.id/repository/danubetech")

    mavenLocal()
    maven("https://repo.danubetech.com/repository/maven-public/")
}

dependencies {
    implementation("io.javalin:javalin-bundle:4.1.1")
    implementation("com.github.kmehrunes:javalin-jwt:0.3")
    implementation("com.beust:klaxon:5.5")

    // SSIKIT
    implementation("id.walt:waltid-ssi-kit:1.1-SNAPSHOT")
    implementation("id.walt:waltid-ssikit-vclib:1.6-SNAPSHOT")

    // Service-Matrix
    implementation("id.walt.servicematrix:WaltID-ServiceMatrix:1.0.1")

    // Testing
    //testImplementation(kotlin("test-junit"))
    testImplementation("io.mockk:mockk:1.12.0")

    testImplementation("io.kotest:kotest-runner-junit5:4.6.3")
    testImplementation("io.kotest:kotest-assertions-core:4.6.3")
    testImplementation("io.kotest:kotest-assertions-json:4.6.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClass.set("id.walt.webwallet.backend.MainKt")
}
