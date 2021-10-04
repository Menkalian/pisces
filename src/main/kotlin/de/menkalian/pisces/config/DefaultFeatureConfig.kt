package de.menkalian.pisces.config

/**
 * Legt die aktuelle Konfiguration der Featuretoggles und Implementationen fest.
 */
class DefaultFeatureConfig : FeatureConfig() {
    override fun configure() {
        pisces.audio.apply {
        }

        pisces.command.apply {
            impl.audio.enableRecursive() // Enable all audio-commands
        }

        pisces.database.apply {
        }

        pisces.discord.apply {
        }

        pisces.message.apply {
        }
    }
}