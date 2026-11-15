plugins {
    kotlin("jvm") version "1.9.25"
    id("com.google.devtools.ksp") version "1.9.25-1.0.20"
    application
}

group = "ru.example.invest"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

dependencies {
    val koraVersion = "1.2.22"

    implementation(platform("ru.tinkoff.kora:kora-parent:$koraVersion"))
    ksp("ru.tinkoff.kora:symbol-processors:$koraVersion")

    implementation("ru.tinkoff.kora:http-server-undertow")
    implementation("ru.tinkoff.kora:json-module")
    implementation("ru.tinkoff.kora:config-hocon")
    implementation("ru.tinkoff.kora:logging-logback")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
}

application {
    mainClass.set("ru.example.invest.ApplicationKt")
}