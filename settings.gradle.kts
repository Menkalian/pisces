rootProject.name = "flunder"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "menkalian-artifactory"
            setUrl("https://artifactory.menkalian.de/artifactory/menkalian")
        }
    }
}
