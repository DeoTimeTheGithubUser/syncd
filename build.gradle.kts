import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    application
}

group = "me.deotime"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.q64.io/rain-public")
    }
    mavenLocal()
}

dependencies {
    testImplementation(kotlin("test"))


    // cli
    implementation("com.github.ajalt.clikt:clikt:3.5.0")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    // ktor
    implementation("io.ktor:ktor-server-core:2.2.1")
    implementation("io.ktor:ktor-server-netty:2.2.1")
    implementation("io.ktor:ktor-client-core:2.2.1")
    implementation("io.ktor:ktor-client-cio:2.2.1")
    implementation("io.ktor:ktor-server-websockets:2.2.1")
    implementation("io.ktor:ktor-client-websockets:2.2.1")

    // logging
    implementation("ch.qos.logback:logback-classic:1.4.5")

    // optics / algebra
    implementation("co.q64.rain:raindrop:1.19-SNAPSHOT")

    // storage
    implementation("me.deotime.warehouse:warehouse:1.0.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes("Main-Class" to "me.deotime.syncd.SyncdKt")
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}

application {
    mainClass.set("me.deotime.syncd.SyncdKt")
}