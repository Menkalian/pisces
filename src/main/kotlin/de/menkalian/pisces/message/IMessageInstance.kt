package de.menkalian.pisces.message

import java.time.temporal.TemporalAccessor

interface IMessageInstance {
    val guildId: Long?
    val channelId: Long
    val messageId: Long

    fun setAuthor(name: String? = null, url: String? = null, iconUrl: String? = null): IMessageInstance
    fun setTitle(title: String? = null): IMessageInstance
    fun setText(text: String? = null): IMessageInstance
    fun addText(text: Any? = ""): IMessageInstance
    fun clearFields(): IMessageInstance
    fun createBlankField(isInline: Boolean): IMessageInstance
    fun createField(name: String = "", text: String = "", isInline: Boolean = false): IMessageInstance
    fun setColor(red: Byte = 0, green: Byte = 0, blue: Byte = 0): IMessageInstance
    fun setTimestamp(timestamp: TemporalAccessor? = null): IMessageInstance
    fun setImage(imageUrl: String? = null): IMessageInstance
    fun setThumbnail(imageUrl: String? = null): IMessageInstance
    fun setFooter(text: String? = null, url: String? = null): IMessageInstance

    fun stopInvalidationTimer()

    fun addReaction(reaction: String)
    fun clearUserReactions(reaction: String)
    fun removeReaction(reaction: String)
    fun removeAllReactions()

    fun addReactionHandler(handler: IMessageHandler.IReactionListener)
    fun removeReactionHandler(handler: IMessageHandler.IReactionListener)

    fun onReactionAdded(reaction: String, action: IReactionAction)
    fun onReactionRemoved(reaction: String, action: IReactionAction)
    fun clearReactionAction(reaction: String)

    fun interface IReactionAction {
        fun onAction(userId: Long, messageInstance: IMessageInstance)
    }
}