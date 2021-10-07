package de.menkalian.pisces.message

import de.menkalian.pisces.discord.IDiscordHandler
import de.menkalian.pisces.message.spec.MessageSpec
import de.menkalian.pisces.util.PiscesColor
import de.menkalian.pisces.util.logger
import java.time.OffsetDateTime

/**
 * Builder zum Erstellen einer neuen Nachricht.
 *
 * @property discordHandler Akutelle Instanz der [IDiscordHandler]-Schnittstelle
 * @property messageHandler Akutelle Instanz der [IMessageHandler]-Schnittstelle
 * @property guildId Discord-ID des Servers, in dem die Nachricht geschickt werden soll.
 *                   Falls dieser Wert `null` ist, wird die Nachricht in einem Privatchat geschickt.
 * @property channelId Discord-ID des Kanals, in dem die Nachricht geschickt werden soll.
 *                     Falls [guildId] `null` ist, wird dieser Wert als UserId interpretiert.
 */
class MessageBuilder(
    private val discordHandler: IDiscordHandler,
    private val messageHandler: IMessageHandler,
    private val guildId: Long?, private val channelId: Long
) : MessageSpec<MessageBuilder>() {
    init {
        clear()
    }

    /**
     * Überträgt die aktuell eingestellten Werte an ein anderes [MessageSpec]-Objekt.
     *
     * @param messageSpec Zielobjekt des Informationstransfers.
     */
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
        messageSpec.withThumbnail(thumbnailUrl)
        messageSpec.withFooter(footerText, footerIconUrl)
    }

    /**
     * Erstellt eine [IMessageInstance] aus den aktuellen Informationen dieses Builders.
     * Mit dem Erstellen der [IMessageInstance] wird die Nachricht auch gesendet.
     */
    fun build(): IMessageInstance {
        return MessageInstance(discordHandler, messageHandler, guildId, channelId, this)
    }

    /**
     * Setzt die Informationen dieses Builders wieder auf die Standardwerte zurück.
     */
    fun clear(): MessageBuilder {
        logger().debug("Clearing $this")
        withAuthor(discordHandler.selfUser.name, "https://pisces.menkalian.de", discordHandler.selfUser.avatarUrl)
        withTitle("Message")
        withText("")
        clearFields()
        withColor(PiscesColor.colorInt)
        withTimestamp(OffsetDateTime.now())
        withImage("")
        withThumbnail("")
        withFooter("Visit Pisces on Gitlab", discordHandler.selfUser.avatarUrl)

        return this
    }

    /**
     * NoOp-Implementierung, da [MessageBuilder] diesen Callback nicht benötigt.
     */
    override fun onUpdated() {}
}