package de.menkalian.pisces.message

import de.menkalian.pisces.IHandler

/**
 * Sch
 */
interface IMessageHandler : IHandler {
    fun createMessage(guildId: Long, channelId: Long) : MessageBuilder
    fun createPrivateMessage(userId: Long) : MessageBuilder
    fun invalidateMessage(messageInstance: IMessageInstance)

    fun addReactionListener(messageInstance: IMessageInstance, listener: IReactionListener)
    fun removeReactionListener(messageInstance: IMessageInstance, listener: IReactionListener)

    interface IReactionListener {
        fun onReactionAdded(userId: Long, messageInstance: IMessageInstance, reaction: String)
        fun onReactionRemoved(userId: Long, messageInstance: IMessageInstance, reaction: String)
    }
}