package de.menkalian.pisces.message

import de.menkalian.pisces.discord.IDiscordHandler
import de.menkalian.pisces.message.spec.FieldSpec
import de.menkalian.pisces.message.spec.MessageSpec
import de.menkalian.pisces.util.Emoji
import de.menkalian.pisces.util.TimeoutTimer
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import java.time.temporal.TemporalAccessor

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

    private val currentPageIncrementAction = { _: Long, _: IMessageInstance ->
        currentPage++
        clearUserReactions(Emoji.ARROW_UP)
        updateRenderedMessage()
    }
    private val currentPageDecrementAction = { _: Long, _: IMessageInstance ->
        currentPage--
        clearUserReactions(Emoji.ARROW_DOWN)
        updateRenderedMessage()
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
                jdaMessageInstance = targetChannel
                    .sendMessageEmbeds(renderMessage())
                    .complete()!!
            } else {
                throw IllegalArgumentException()
            }
        } else {
            val targetChannel = discordHandler.jda
                .getUserById(channelId)
                ?.openPrivateChannel()
                ?.complete()
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
        inactivityTimer.stop()
    }

    override fun addReaction(reaction: String) {
        if (hasReactionRights(false))
            jdaMessageInstance.addReaction(reaction).complete()
    }

    override fun clearUserReactions(reaction: String) {
        if (hasReactionRights(true)) {
            removeReaction(reaction)
            addReaction(reaction)
        }
    }

    override fun removeReaction(reaction: String) {
        if (hasReactionRights(true)) {
            jdaMessageInstance.clearReactions(reaction).complete()
        }
    }

    override fun removeAllReactions() {
        if (hasReactionRights(true))
            jdaMessageInstance.clearReactions()
    }

    override fun addReactionHandler(handler: IMessageHandler.IReactionListener) {
        messageHandler.addReactionListener(this, handler)
    }

    override fun removeReactionHandler(handler: IMessageHandler.IReactionListener) {
        messageHandler.removeReactionListener(this, handler)
    }

    override fun onReactionAdded(reaction: String, action: IMessageInstance.IReactionAction) {
        if (reactionAddedActions.containsKey(reaction).not()) {
            reactionAddedActions[reaction] = mutableListOf()
        }
        reactionAddedActions[reaction]?.add(action)
    }

    override fun onReactionRemoved(reaction: String, action: IMessageInstance.IReactionAction) {
        if (reactionRemovedActions.containsKey(reaction).not()) {
            reactionRemovedActions[reaction] = mutableListOf()
        }
        reactionRemovedActions[reaction]?.add(action)
    }

    override fun clearReactionAction(reaction: String) {
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

    private fun updateRenderedMessage() {
        synchronized(messageEditMutex) {
            jdaMessageInstance
                .editMessageEmbeds(renderMessage())
                .complete()
            updateScrollReactions()
        }
    }

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

    private fun updateScrollReactions() {
        val pageRange = 0 until pages.size
        currentPage = currentPage.coerceIn(pageRange)

        if (currentPage != pageRange.first) {
            addReaction(Emoji.ARROW_DOWN)
            onReactionAdded(Emoji.ARROW_DOWN, currentPageDecrementAction)
        } else {
            clearReactionAction(Emoji.ARROW_DOWN)
        }

        if (currentPage != pageRange.last) {
            addReaction(Emoji.ARROW_UP)
            onReactionAdded(Emoji.ARROW_UP, currentPageIncrementAction)
        } else {
            clearReactionAction(Emoji.ARROW_UP)
        }
    }

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

    private fun GuildChannel.isEligibleChannel(): Boolean {
        return this.type.isMessage
                && guild.getMember(jda.selfUser)?.hasPermission(this, Permission.MESSAGE_WRITE) ?: false
    }
}