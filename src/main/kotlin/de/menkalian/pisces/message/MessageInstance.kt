package de.menkalian.pisces.message

import de.menkalian.pisces.discord.IDiscordHandler
import de.menkalian.pisces.message.spec.FieldSpec
import de.menkalian.pisces.message.spec.MessageSpec
import de.menkalian.pisces.util.Emoji
import de.menkalian.pisces.util.TimeoutTimer
import de.menkalian.pisces.util.logger
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import java.time.temporal.TemporalAccessor

/**
 * Repräsentation einer Nachrichteninstanz, die bearbeitet werden kann.
 * Falls nach einer festgelegten Zeitspanne (30 min) keine Interaktion mit der Nachricht stattgefunden hat, wird die Instanz ungültig und verworfen.
 * Falls keine Interaktion mit der Nachricht nötig ist, sollte direkt nach der Erstellung (bzw. nachdem keine Interaktion mehr erforderlich ist) die Funktion [stopInvalidationTimer] aufgerufen werden.
 *
 * @property discordHandler Akutelle Instanz der [IDiscordHandler]-Schnittstelle
 * @property messageHandler Akutelle Instanz der [IMessageHandler]-Schnittstelle
 * @property guildId Discord-ID des Servers, in dem die Nachricht geschickt werden soll.
 *                   Falls dieser Wert `null` ist, wird die Nachricht in einem Privatchat geschickt.
 * @property channelId Discord-ID des Kanals, in dem die Nachricht geschickt werden soll.
 *                     Falls [guildId] `null` ist, wird dieser Wert als UserId interpretiert.
 *
 * @property messageEditMutex Mutex zum Absichern der Nachrichtenbearbeitung
 * @property pagesMutex Mutex zum Absichern von Änderungen an [pages]
 *
 * @property initialized Flag, ob die Initialisierung bereits abgeschlossen ist (und die Nachricht initial gesendet worden ist).
 *
 * @property jdaMessageInstance JDA-Instanz der gesendeten Nachricht
 * @property inactivityTimer Timer zur Invalidierung der Nachricht nach Ablauf der festgelegten Zeitspanne.
 *
 * @property pages Interne Aufteilung der Nachricht in Seiten, um die Limits von Discord korrekt zu behandeln.
 * @property currentPage Index der aktuell angezeigten Seite.
 *
 * @property currentPageIncrementAction [IMessageInstance.IReactionAction], die ausgelöst wird, wenn der Nutzer die nächste Seite der Nachricht sehen möchte.
 * @property currentPageDecrementAction [IMessageInstance.IReactionAction], die ausgelöst wird, wenn der Nutzer die vorige Seite der Nachricht sehen möchte.
 *
 * @property reactionAddedActions Aktionen, die beim Hinzufügen einer bestimmten Reaktion ausgelöst werden
 * @property reactionRemovedActions Aktionen, die beim Entfernen einer bestimmten Reaktion ausgelöst werden
 * @property messageReactionListener [IMessageHandler.IReactionListener]-Instanz, um [reactionAddedActions] und [reactionRemovedActions] auszulösen.
 */
class MessageInstance(
    private val discordHandler: IDiscordHandler,
    private val messageHandler: IMessageHandler,
    override val guildId: Long?, override val channelId: Long,
    builder: MessageBuilder
) : MessageSpec<MessageInstance>(), IMessageInstance {

    private data class MessagePage(
        var text: String = "",
        val fields: MutableList<FieldSpec> = mutableListOf()
    )

    private val messageEditMutex = Any()
    private val pagesMutex = Any()

    private var initialized: Boolean = false
    private val jdaMessageInstance: Message
    override val messageId: Long
        get() = jdaMessageInstance.idLong

    private val inactivityTimer = TimeoutTimer(30 * 60 * 1000L) { // Message invalid after 30 minutes without interaction
        messageHandler.invalidateMessage(this)
    }

    private val pages = mutableListOf<MessagePage>()
    private var currentPage = 0

    private val currentPageIncrementAction = { uid: Long, _: IMessageInstance ->
        if (uid != discordHandler.jda.selfUser.idLong) {
            currentPage++
            clearUserReactions(Emoji.ARROW_UP)
            updateRenderedMessage()
        }
    }
    private val currentPageDecrementAction = { uid: Long, _: IMessageInstance ->
        if (uid != discordHandler.jda.selfUser.idLong) {
            currentPage--
            clearUserReactions(Emoji.ARROW_DOWN)
            updateRenderedMessage()
        }
    }

    private val reactionAddedActions: MutableMap<String, MutableList<IMessageInstance.IReactionAction>> = mutableMapOf()
    private val reactionRemovedActions: MutableMap<String, MutableList<IMessageInstance.IReactionAction>> = mutableMapOf()
    private val messageReactionListener = object : IMessageHandler.IReactionListener {
        override fun onReactionAdded(userId: Long, messageInstance: IMessageInstance, reaction: String) {
            inactivityTimer.reset()
            reactionAddedActions[reaction]?.forEach { it.onAction(userId, messageInstance) }
        }

        override fun onReactionRemoved(userId: Long, messageInstance: IMessageInstance, reaction: String) {
            inactivityTimer.reset()
            reactionRemovedActions[reaction]?.forEach { it.onAction(userId, messageInstance) }
        }
    }

    init {
        // Take values from the builder
        builder.applyTo(this)
        buildPages()

        if (guildId != null) {
            // Send as guildMessage
            val targetChannel = discordHandler.jda
                .getGuildById(guildId)
                ?.getGuildChannelById(channelId)
            if (targetChannel?.isEligibleChannel() == true && targetChannel is TextChannel) {
                logger().info("Sending message to $targetChannel")
                jdaMessageInstance = targetChannel
                    .sendMessageEmbeds(renderMessage())
                    .complete()!!
            } else {
                throw IllegalArgumentException("Provided IDs (guild=$guildId, channel=$channelId) are not a valid target.")
            }
        } else {
            val targetChannel = discordHandler.jda
                .getUserById(channelId)
                ?.openPrivateChannel()
                ?.complete()
            logger().info("Sending message to $targetChannel")
            jdaMessageInstance = targetChannel
                ?.sendMessageEmbeds(renderMessage())
                ?.complete()!!
        }

        updateScrollReactions()
        messageHandler.addReactionListener(this, messageReactionListener)

        initialized = true
    }

    //region message manipulation
    override fun setAuthor(name: String?, url: String?, iconUrl: String?): IMessageInstance {
        return withAuthor(name, url, iconUrl)
    }

    override fun setTitle(title: String?): IMessageInstance {
        return withTitle(title)
    }

    override fun setText(text: String?): IMessageInstance {
        return withText(text)
    }

    override fun addText(text: Any?): IMessageInstance {
        return appendText(text)
    }

    override fun createBlankField(isInline: Boolean): IMessageInstance {
        return addBlankField(isInline)
    }

    override fun createField(name: String, text: String, isInline: Boolean): IMessageInstance {
        return addField(name, text, isInline)
    }

    override fun setColor(red: Byte, green: Byte, blue: Byte): IMessageInstance {
        return withColor(red, green, blue)
    }

    override fun setTimestamp(timestamp: TemporalAccessor?): IMessageInstance {
        return withTimestamp(timestamp)
    }

    override fun setImage(imageUrl: String?): IMessageInstance {
        return withImage(imageUrl)
    }

    override fun setThumbnail(imageUrl: String?): IMessageInstance {
        return withThumbnail(imageUrl)
    }

    override fun setFooter(text: String?, url: String?): IMessageInstance {
        return withFooter(text, url)
    }
    //endregion

    override fun stopInvalidationTimer() {
        logger().debug("Stopped invalidation-timer for $jdaMessageInstance")
        inactivityTimer.stop()
    }

    override fun addReaction(reaction: String) {
        if (hasReactionRights(false)) {
            logger().debug("Adding reaction $reaction to $jdaMessageInstance")
            jdaMessageInstance.addReaction(reaction).complete()
        }
    }

    override fun clearUserReactions(reaction: String) {
        if (hasReactionRights(true)) {
            removeReaction(reaction)
            addReaction(reaction)
        }
    }

    override fun removeReaction(reaction: String) {
        if (hasReactionRights(true)) {
            logger().debug("Removing all reactions $reaction from $jdaMessageInstance")
            jdaMessageInstance.clearReactions(reaction).complete()
        }
    }

    override fun removeAllReactions() {
        if (hasReactionRights(true)) {
            logger().debug("Removing all reactions from $jdaMessageInstance")
            jdaMessageInstance.clearReactions()
        }
    }

    override fun addReactionHandler(handler: IMessageHandler.IReactionListener) {
        messageHandler.addReactionListener(this, handler)
    }

    override fun removeReactionHandler(handler: IMessageHandler.IReactionListener) {
        messageHandler.removeReactionListener(this, handler)
    }

    override fun addOnReactionAddedAction(reaction: String, action: IMessageInstance.IReactionAction) {
        if (reactionAddedActions.containsKey(reaction).not()) {
            reactionAddedActions[reaction] = mutableListOf()
        }
        reactionAddedActions[reaction]?.add(action)
    }

    override fun addOnReactionRemovedAction(reaction: String, action: IMessageInstance.IReactionAction) {
        if (reactionRemovedActions.containsKey(reaction).not()) {
            reactionRemovedActions[reaction] = mutableListOf()
        }
        reactionRemovedActions[reaction]?.add(action)
    }

    override fun clearReactionActions(reaction: String) {
        reactionAddedActions[reaction]?.clear()
        reactionRemovedActions[reaction]?.clear()
    }

    override fun onUpdated() {
        if (initialized) {
            inactivityTimer.reset()
            buildPages()
            currentPage = currentPage.coerceIn(0 until pages.size)
            updateRenderedMessage()
        }
    }

    /**
     * Bearbeitet die gesendete Nachricht, so dass diese zu den aktuellen Werten passt.
     * Dies kann beispielsweise aufgerufen werden nachdem sich [currentPage] geändert hat.
     */
    private fun updateRenderedMessage() {
        synchronized(messageEditMutex) {
            jdaMessageInstance
                .editMessageEmbeds(renderMessage())
                .complete()
            updateScrollReactions()
        }
    }

    /**
     * Berechnet die Seiten, abhängig von den aktuellen Einstellungen der Nachricht und speichert diese in [pages].
     * Diese Methode läuft synchronisiert durch [pagesMutex]
     */
    private fun buildPages() {
        synchronized(pagesMutex) {
            pages.clear()

            val baseLength = title.length + author.name.length + footerText.length
            val lengthPerPage = MessageEmbed.EMBED_MAX_LENGTH_BOT - baseLength

            var remainingLength: Int
            var currentPage = MessagePage()

            // Split the text
            val chunkedText = text.chunked(lengthPerPage)
            chunkedText.take(chunkedText.size - 1).forEach {
                pages.add(MessagePage(it))
            }
            remainingLength = lengthPerPage - chunkedText.last().length
            currentPage.text = chunkedText.last()

            fields.forEach {
                if (it.length > remainingLength) {
                    pages.add(currentPage)
                    currentPage = MessagePage()
                    remainingLength = lengthPerPage
                }
                currentPage.fields.add(it)
                remainingLength -= it.length
            }

            // Add the final page
            pages.add(currentPage)
        }
    }

    /**
     * Überträgt die aktuellen Werte auf ein [MessageEmbed], das dann an Discord geschickt werden kann.
     * Diese Methode läuft teilweise synchronisiert durch [pagesMutex]
     */
    private fun renderMessage(): MessageEmbed {
        val builder = EmbedBuilder()

        builder.setTitle(title)
        builder.setColor(color)
        builder.setTimestamp(timestamp)

        if (imageUrl.isNotBlank())
            builder.setImage(imageUrl)
        if (thumbnailUrl.isNotBlank())
            builder.setThumbnail(thumbnailUrl)
        if (footerUrl.isNotBlank())
            builder.setFooter(footerText, footerUrl)
        else
            builder.setFooter(footerText)

        synchronized(pagesMutex) {
            val currPageSpec = pages.getOrElse(currentPage) { MessagePage() }
            builder.setDescription(currPageSpec.text)
            currPageSpec.fields.forEach {
                if (it.blank)
                    builder.addBlankField(it.inline)
                else
                    builder.addField(it.title, it.text, it.inline)
            }
        }

        return builder.build()
    }

    /**
     * Aktualisiert die Reaktionen zum Seitenwechsel.
     * Zudem wird durch diese Methode sichergestellt, dass der aktuelle Wert von [currentPage] gültig ist.
     */
    private fun updateScrollReactions() {
        val pageRange = 0 until pages.size
        currentPage = currentPage.coerceIn(pageRange)

        if (currentPage != pageRange.first) {
            addReaction(Emoji.ARROW_DOWN)
            addOnReactionAddedAction(Emoji.ARROW_DOWN, currentPageDecrementAction)
        } else {
            removeReaction(Emoji.ARROW_DOWN)
            clearReactionActions(Emoji.ARROW_DOWN)
        }

        if (currentPage != pageRange.last) {
            addReaction(Emoji.ARROW_UP)
            addOnReactionAddedAction(Emoji.ARROW_UP, currentPageIncrementAction)
        } else {
            removeReaction(Emoji.ARROW_UP)
            clearReactionActions(Emoji.ARROW_UP)
        }
    }

    /**
     * Prüft ob der aktuelle Nutzeraccount Reaktionen zu dieser Nachricht hinzufügen kann.
     * @param checkRemove Ob zusätzlich geprüft werden soll, dass Reaktionen gelöscht werden können.
     */
    private fun hasReactionRights(checkRemove: Boolean): Boolean {
        if (jdaMessageInstance.channelType.isGuild) {
            val guildMember = jdaMessageInstance.guild.getMember(discordHandler.jda.selfUser)
            return jdaMessageInstance.channelType.isGuild
                    && guildMember?.hasPermission(jdaMessageInstance.textChannel, Permission.MESSAGE_ADD_REACTION) == true
                    && (!checkRemove || guildMember.hasPermission(jdaMessageInstance.textChannel, Permission.MESSAGE_MANAGE))
        } else
        // for private channels you can always add but may never remove emotes
            return !checkRemove
    }

    /**
     * Prüft, ob der Channel ein gültiges Ziel für das Senden von Nachrichten ist.
     */
    private fun GuildChannel.isEligibleChannel(): Boolean {
        return this.type.isMessage
                && guild.getMember(jda.selfUser)?.hasPermission(this, Permission.MESSAGE_WRITE) ?: false
    }
}