package de.menkalian.pisces.message

import de.menkalian.pisces.discord.IDiscordHandler
import de.menkalian.pisces.message.spec.MessageSpec
import de.menkalian.pisces.util.PiscesColor

class MessageBuilder(
    private val discordHandler: IDiscordHandler,
    private val messageHandler: IMessageHandler,
    private val guildId: Long?, private val channelId: Long
) : MessageSpec<MessageBuilder>() {
    init {
        clear()
    }

    internal fun applyTo(messageSpec: MessageSpec<*>) {
        messageSpec.withAuthor(author.name, author.url, author.iconUrl)
        messageSpec.withTitle(title)
        messageSpec.withText(text)

        fields.forEach {
            if (it.blank)
                messageSpec.addBlankField(it.inline)
            else
                messageSpec.addField(it.title, it.text, it.inline)
        }

        messageSpec.withColor(color)
        messageSpec.withTimestamp(timestamp)
        messageSpec.withImage(imageUrl)
        messageSpec.withThumbnail(imageUrl)
        messageSpec.withFooter(footerText, footerUrl)
    }

    fun build(): IMessageInstance {
        return MessageInstance(discordHandler, messageHandler, guildId, channelId, this)
    }

    fun clear(): MessageBuilder {
        withAuthor(discordHandler.jda.selfUser.name, "https://pisces.menkalian.de", discordHandler.jda.selfUser.effectiveAvatarUrl)
        withTitle("Message")
        withColor(PiscesColor.colorInt)
        withFooter("Visit Pisces on Gitlab", "https://gitlab.com/kiliankra/pisces")

        return this
    }

    override fun onUpdated() {}
}