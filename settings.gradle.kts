rootProject.name = "flunder"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "menkalian-artifactory"
            setUrl("http://server.menkalian.de:8081/artifactory/menkalian")
            isAllowInsecureProtocol = true
        }
    }
}
