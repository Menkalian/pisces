package de.menkalian.pisces.config

/**
 * Legt die aktuelle Konfiguration der Featuretoggles und Implementationen fest.
 */
class DefaultFeatureConfig : FeatureConfig() {
    override fun configure() {
        pisces.audio.apply {
            spotify.SpotifyHelper.enable()
        }

        pisces.command.apply {
            impl.audio.enableRecursive() // Enable all audio-commands
            impl.settings.enableRecursive()
        }

        pisces.database.apply {
        }

        pisces.discord.apply {
        }

        pisces.message.apply {
        }
    }
}