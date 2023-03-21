@file:Suppress("GradlePackageUpdate")

plugins {
    // Spring
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"

    // Languages
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.8.0"
    kotlin("plugin.jpa") version "1.8.0"
    java

    // Gradle utilities
    id("org.jetbrains.dokka") version "1.7.20"
    jacoco
    `maven-publish`

    // Menkalian/Utilities
    id("de.menkalian.vela.buildconfig") version "1.0.1"
    id("de.menkalian.vela.keygen") version "1.2.1"
    id("de.menkalian.vela.featuretoggle") version "1.0.1"
}

group = "de.menkalian.pisces"
version = "5.5.4"

// Compilation and generation settings
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets.get("main").resources {
    srcDir(File(buildDir, "external/resources"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlin.RequiresOptIn")
        jvmTarget = "17"
    }
}

tasks.getByName("kotlinSourcesJar") {
    dependsOn(tasks.generateBuildConfig)
    dependsOn(tasks.generateKeyObjects)
    dependsOn(tasks.generateFeaturetoggleCode)
}

rootProject.project("frontend").afterEvaluate {
    val flunder = rootProject
    val frontend = this
    flunder.tasks.create("copyFrontend", Copy::class.java) {
        from(frontend.file("dist"))
        destinationDir = File(flunder.buildDir, "external/resources/static").apply { mkdirs() }

        dependsOn(frontend.tasks.getByName("npm_run_build"))
        flunder.tasks.getByName("processResources").dependsOn(this)
    }
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
        setUrl("https://artifactory.menkalian.de/artifactory/menkalian")
    }
}

publishing {
    repositories {
        maven {
            name = "artifactory-menkalian"
            setUrl("https://artifactory.menkalian.de/artifactory/pisces")
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
    implementation(springboot("websocket"))
    implementation(springboot("security"))
    implementation(springboot("oauth2-client"))
    implementation(springboot("actuator"))
    implementation(springboot("data-jpa"))

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")

    // JDA
    implementation("net.dv8tion:JDA:4.4.1_353")
    implementation("com.sedmelluq:lavaplayer:1.3.78")

    // Spotify
    implementation("se.michaelthelin.spotify:spotify-web-api-java:6.5.4")

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
