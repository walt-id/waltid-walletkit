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
    implementation("io.javalin:javalin-bundle:4.3.0")
    implementation("com.github.kmehrunes:javalin-jwt:0.3")
    implementation("com.beust:klaxon:5.5")
    implementation("com.nimbusds:oauth2-oidc-sdk:9.21")

    // SSIKIT
    implementation("id.walt:waltid-ssi-kit:1.6.1")
    implementation("id.walt:waltid-ssikit-vclib:1.13.0")

    // Service-Matrix
    implementation("id.walt.servicematrix:WaltID-ServiceMatrix:1.1.0")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.0-alpha6")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha6")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")

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
