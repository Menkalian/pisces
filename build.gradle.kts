plugins {
    // Spring
    id("org.springframework.boot") version "2.5.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    // Languages
    kotlin("jvm") version "1.5.21"
    kotlin("plugin.spring") version "1.5.21"
    kotlin("plugin.jpa") version "1.5.21"
    java

    // Menkalian/Utilities
    id("de.menkalian.vela.buildconfig") version "1.0.0"
    id("de.menkalian.vela.keygen") version "1.2.1"
    id("de.menkalian.vela.versioning") version "1.1.0"
    id("de.menkalian.vela.featuretoggle") version "1.0.0"
}

group = "de.menkalian"
version = "5.0.0"

repositories {
    mavenCentral()
    maven {
        name = "jda-maven"
        setUrl("https://m2.dv8tion.net/releases")
    }
    maven {
        name = "menkalian-artifactory"
        setUrl("http://server.menkalian.de:8081/artifactory/menkalian")
        isAllowInsecureProtocol = true
    }
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    // Spring
    val springboot = {module: String -> "org.springframework.boot:spring-boot-starter-$module"}
    implementation(springboot("web"))
    implementation(springboot("actuator"))
    implementation(springboot("data-jpa"))

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    runtimeOnly("org.hsqldb:hsqldb")

    // JDA
    implementation("net.dv8tion:JDA:4.3.0_310")
    implementation("com.sedmelluq:lavaplayer:1.3.78")

    testImplementation(springboot("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

java.sourceCompatibility = JavaVersion.VERSION_11

keygen {
    targetPackage = "de.menkalian.pisces.variables"
}

featuretoggle {
    targetPackage = "de.menkalian.pisces.config"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}