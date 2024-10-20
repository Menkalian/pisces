package de.menkalian.pisces.message

import de.menkalian.pisces.discord.IDiscordHandler
import de.menkalian.pisces.message.spec.FieldSpec
import de.menkalian.pisces.message.spec.MessageSpec
import de.menkalian.pisces.util.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
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
        currentPage = 0
        messageHandler.invalidateMessage(this)
    }

    private val pages = mutableListOf<MessagePage>()
    private var currentPage = 0

    private val currentPageIncrementAction = { uid: Long, _: IMessageInstance ->
        if (uid != discordHandler.selfUser.id) {
            currentPage++
            updateRenderedMessage()
            true
        } else {
            false
        }
    }
    private val currentPageDecrementAction = { uid: Long, _: IMessageInstance ->
        if (uid != discordHandler.selfUser.id) {
            currentPage--
            updateRenderedMessage()
            true
        } else {
            false
        }
    }

    private val reactionAddedActions: MutableMap<String, MutableList<IMessageInstance.IReactionAction>> = mutableMapOf()
    private val reactionRemovedActions: MutableMap<String, MutableList<IMessageInstance.IReactionAction>> = mutableMapOf()
    private val messageReactionListener = object : IMessageHandler.IReactionListener {
        override fun onReactionAdded(userId: Long, messageInstance: IMessageInstance, reaction: String): Boolean {
            inactivityTimer.reset()
            return reactionAddedActions[reaction]?.any { it.onAction(userId, messageInstance) } ?: false
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

        if (guildId != null && guildId != 0L) {
            // Send as guildMessage
            val targetChannel = discordHandler
                .getJdaGuild(guildId)
                ?.getGuildChannelById(channelId)
            if (targetChannel?.isEligibleChannel() == true && targetChannel is GuildMessageChannel) {
                logger().info("Sending $this to $targetChannel")
                jdaMessageInstance = targetChannel
                    .sendMessageEmbeds(renderMessage())
                    .complete()!!
                logger().debug("Successfully sent $this as message ${jdaMessageInstance.idLong}")
            } else {
                throw IllegalArgumentException("Provided IDs (guild=$guildId, channel=$channelId) are not a valid target.")
            }
        } else {
            val targetChannel = discordHandler
                .getJdaUser(channelId)
                ?.openPrivateChannel()
                ?.complete()
            logger().info("Sending $this to $targetChannel")
            jdaMessageInstance = targetChannel
                ?.sendMessageEmbeds(renderMessage())
                ?.complete()!!
            logger().debug("Successfully sent $this as message ${jdaMessageInstance.idLong}")
        }

        messageHandler.addReactionListener(this, messageReactionListener)
        updateScrollReactions()

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
        logger().debug("Stopped invalidation-timer for $this")
        inactivityTimer.stop()
    }

    override fun addReaction(reaction: String) {
        if (hasReactionRights(false)) {
            logger().debug("Adding reaction $reaction to $this")
            jdaMessageInstance.addReaction(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode(reaction)).complete()
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
            logger().debug("Removing all reactions \"$reaction\" from $this")
            jdaMessageInstance.clearReactions(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode(reaction)).complete()
        }
    }

    override fun removeAllReactions() {
        if (hasReactionRights(true)) {
            logger().debug("Removing all reactions from $this")
            jdaMessageInstance.clearReactions().complete()
        }
    }

    override fun addReactionHandler(handler: IMessageHandler.IReactionListener) {
        messageHandler.addReactionListener(this, handler)
    }

    override fun removeReactionHandler(handler: IMessageHandler.IReactionListener) {
        messageHandler.removeReactionListener(this, handler)
    }

    override fun addOnReactionAddedAction(reaction: String, action: IMessageInstance.IReactionAction) {
        synchronized(reactionAddedActions) {
            if (reactionAddedActions.containsKey(reaction).not()) {
                reactionAddedActions[reaction] = mutableListOf()
            }
            if (reactionAddedActions[reaction]?.contains(action)?.not() != false) {
                reactionAddedActions[reaction]?.add(action)
            }
        }
    }

    override fun addOnReactionRemovedAction(reaction: String, action: IMessageInstance.IReactionAction) {
        synchronized(reactionRemovedActions) {
            if (reactionRemovedActions.containsKey(reaction).not()) {
                reactionRemovedActions[reaction] = mutableListOf()
            }
            if (reactionRemovedActions[reaction]?.contains(action)?.not() != false) {
                reactionRemovedActions[reaction]?.add(action)
            }
        }
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
            logger().info("Updating $this")
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
            logger().debug("Rebuilding pages for $this")
            pages.clear()

            val baseLength = title.length + author.name.length + footerText.length
            // For some reason discord only displays TEXT_MAX_LENGTH (also 6k characters are very hard to properly read, so 2k are better for this)
            val lengthPerPage = minOf(MessageEmbed.TEXT_MAX_LENGTH, MessageEmbed.EMBED_MAX_LENGTH_BOT) - baseLength

            var remainingLength: Int
            var currentPage = MessagePage()

            // Split the text
            val chunkedText = text.chunked(lengthPerPage)
            chunkedText.take((chunkedText.size - 1).coerceAtLeast(0)).forEach {
                pages.add(MessagePage(it))
            }

            remainingLength = lengthPerPage - (chunkedText.lastOrNull()?.length ?: 0)
            var remainingCount = MessageEmbed.MAX_FIELD_AMOUNT
            currentPage.text = chunkedText.lastOrNull() ?: ""

            fields.forEach {
                if (it.length > remainingLength || remainingCount <= 0) {
                    pages.add(currentPage)
                    currentPage = MessagePage()
                    remainingLength = lengthPerPage
                    remainingCount = MessageEmbed.MAX_FIELD_AMOUNT
                }
                currentPage.fields.add(it)
                remainingLength -= it.length
                remainingCount -= 1
            }

            // Add the final page
            pages.add(currentPage)
            logger().trace("Built ${pages.size} page(s) $pages")
        }
    }

    /**
     * Überträgt die aktuellen Werte auf ein [MessageEmbed], das dann an Discord geschickt werden kann.
     * Diese Methode läuft teilweise synchronisiert durch [pagesMutex]
     */
    private fun renderMessage(): MessageEmbed {
        logger().debug("Creating MessageEmbed from $this")
        val builder = EmbedBuilder()

        builder.setTitle(title)
        builder.setColor(color)
        builder.setTimestamp(timestamp)

        if (imageUrl.isNotBlank())
            builder.setImage(imageUrl)
        if (thumbnailUrl.isNotBlank())
            builder.setThumbnail(thumbnailUrl)
        if (footerIconUrl.isNotBlank())
            builder.setFooter(footerText, footerIconUrl)
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
        logger().debug("$this displays currently page #$currentPage")

        if (currentPage != pageRange.first) {
            addReaction(Emoji.ARROW_UP)
            addOnReactionAddedAction(Emoji.ARROW_UP, currentPageDecrementAction)
        } else {
            removeReaction(Emoji.ARROW_UP)
        }

        if (currentPage != pageRange.last) {
            addReaction(Emoji.ARROW_DOWN)
            addOnReactionAddedAction(Emoji.ARROW_DOWN, currentPageIncrementAction)
        } else {
            removeReaction(Emoji.ARROW_DOWN)
        }
    }

    /**
     * Prüft ob der aktuelle Nutzeraccount Reaktionen zu dieser Nachricht hinzufügen kann.
     * @param checkRemove Ob zusätzlich geprüft werden soll, dass Reaktionen gelöscht werden können.
     */
    private fun hasReactionRights(checkRemove: Boolean): Boolean {
        if (jdaMessageInstance.channelType.isGuild) {
            val guildMember = jdaMessageInstance.guild.getMemberById(discordHandler.selfUser.id)
            return jdaMessageInstance.channelType.isGuild
                    && guildMember?.hasPermission(jdaMessageInstance.guildChannel, Permission.MESSAGE_ADD_REACTION) == true
                    && (!checkRemove || guildMember.hasPermission(jdaMessageInstance.guildChannel, Permission.MESSAGE_MANAGE))
        } else
        // for private channels you can always add but may never remove emotes
            return !checkRemove
    }

    /**
     * Prüft, ob der Channel ein gültiges Ziel für das Senden von Nachrichten ist.
     */
    private fun GuildChannel.isEligibleChannel(): Boolean {
        return this.type.isMessage
                && guild.getMember(jda.selfUser)?.hasPermission(this, Permission.MESSAGE_SEND) ?: false
    }
}