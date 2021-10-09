@file:Suppress("GradlePackageUpdate")

plugins {
    // Spring
    id("org.springframework.boot") version "2.5.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    // Languages
    kotlin("jvm") version "1.5.21"
    kotlin("plugin.spring") version "1.5.21"
    kotlin("plugin.jpa") version "1.5.21"
    java

    // Gradle utilities
    id("org.jetbrains.dokka") version "1.5.0"
    jacoco
    `maven-publish`

    // Menkalian/Utilities
    id("de.menkalian.vela.buildconfig") version "1.0.1"
    id("de.menkalian.vela.keygen") version "1.2.1"
    id("de.menkalian.vela.featuretoggle") version "1.0.1"
}

group = "de.menkalian.pisces"
version = "5.1.1"

// Compilation and generation settings
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlin.RequiresOptIn")
        jvmTarget = "11"
    }
}
tasks.getByName("kotlinSourcesJar") {
    dependsOn(tasks.generateBuildConfig)
    dependsOn(tasks.generateKeyObjects)
    dependsOn(tasks.generateFeaturetoggleCode)
}
tasks.bootJar.configure {
    archiveClassifier.set("boot")
}

keygen {
    targetPackage = "de.menkalian.pisces.variables"
    finalLayerAsString = true
}
featuretoggle {
    targetPackage = "de.menkalian.pisces.config"
}

// Repository settings
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
publishing {
    repositories {
        maven {
            url = uri("http://server.menkalian.de:8081/artifactory/pisces")
            name = "artifactory-menkalian"
            isAllowInsecureProtocol = true
            credentials {
                username = System.getenv("MAVEN_REPO_USER")
                password = System.getenv("MAVEN_REPO_PASS")
            }
        }
    }
}

// Dependencies
dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    // Spring
    val springboot = { module: String -> "org.springframework.boot:spring-boot-starter-$module" }
    implementation(springboot("web"))
    implementation(springboot("actuator"))
    implementation(springboot("data-jpa"))

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")

    // JDA
    implementation("net.dv8tion:JDA:4.3.0_334")
    implementation("com.sedmelluq:lavaplayer:1.3.78")

    testImplementation(springboot("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

// Test/Verification settings
tasks.getByName<Test>("test") {
    testLogging.showStandardStreams = true
    useJUnitPlatform()
}
tasks.withType<JacocoReport>().configureEach {
    dependsOn(tasks.getByName("test"))
    reports {
        xml.required.set(true)
        csv.required.set(true)
    }
}
tasks.getByName("check") {
    dependsOn(tasks.withType<JacocoReport>())
}

// Documentation
tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    dependsOn(tasks.generateBuildConfig)
    dependsOn(tasks.generateKeyObjects)
    dependsOn(tasks.generateFeaturetoggleCode)
    dokkaSourceSets {
        named("main") {
            includes.from(file("src/main/resources/modules.dokka.md"))
            moduleName.set("Pisces (DJ Flunder) Source Dokumentation")
            platform.set(org.jetbrains.dokka.Platform.jvm)
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(uri("https://gitlab.com/kiliankra/pisces/-/tree/main/src/main/kotlin").toURL())
                remoteLineSuffix.set("#L")
            }
        }
    }
}
val dokkaHtmlJarTask = tasks.create("dokkaHtmlJar", org.gradle.jvm.tasks.Jar::class.java) {
    archiveClassifier.set("dokka")
    from(tasks.dokkaHtml)
}

// Deployment
publishing {
    publications {
        create<MavenPublication>("dj-flunder") {
            artifactId = "pisces"
            from(components["java"])
            artifact(tasks.kotlinSourcesJar)
            artifact(tasks.bootJar)
            artifact(dokkaHtmlJarTask)
        }
    }
}
