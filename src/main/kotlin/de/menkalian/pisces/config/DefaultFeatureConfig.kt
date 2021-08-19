package de.menkalian.pisces.config

class DefaultFeatureConfig : FeatureConfig() {
    override fun configure() {
        pisces.audio.apply {
            Handler.enable()
        }

        pisces.command.apply {
            Handler.enable()
        }

        pisces.database.apply {
            Handler.enable()
        }

        pisces.discord.apply {
            Handler.enable()
        }

        pisces.message.apply {
            Handler.enable()
        }
    }
}