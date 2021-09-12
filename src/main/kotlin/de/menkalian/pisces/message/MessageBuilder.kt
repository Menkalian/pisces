package de.menkalian.pisces.message

import de.menkalian.pisces.discord.IDiscordHandler

class MessageBuilder(val discordHandler: IDiscordHandler, val guildId: Long, val channelId: Long) : MessageSpec<MessageBuilder>() {
    init {
        clear()
    }

    fun clear(): MessageBuilder {
        withAuthor(discordHandler.jda.selfUser.name, "https://pisces.menkalian.de", discordHandler.jda.selfUser.effectiveAvatarUrl)
        withTitle("Message")

        return this
    }

    override fun onUpdated() {}
}