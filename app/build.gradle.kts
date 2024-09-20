plugins {
    kotlin("jvm")
    id("application")
}

application {
    mainClass = "com.github.enteraname74.info901project.app.Main"
}

group = "com.github.enteraname74.info901project"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":communication"))
    implementation(project(":domain"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}