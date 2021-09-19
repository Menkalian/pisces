package de.menkalian.pisces.message

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Service

@Service
class MessageReactionListener : ListenerAdapter() {
    private val instances: MutableMap<Long, IMessageInstance> = mutableMapOf()
    private val listeners: MutableMap<Long, MutableList<IMessageHandler.IReactionListener>> = mutableMapOf()

    fun addListener(instance: IMessageInstance, listener: IMessageHandler.IReactionListener) {
        instances[instance.messageId] = instance
        if (listeners.containsKey(instance.messageId).not())
            listeners[instance.messageId] = mutableListOf()
        listeners[instance.messageId]?.add(listener)
    }

    fun removeListener(instance: IMessageInstance, listener: IMessageHandler.IReactionListener) {
        listeners[instance.messageId]?.remove(listener)
    }

    fun removeListeners(instance: IMessageInstance) {
        instances.remove(instance.messageId)
        listeners.remove(instance.messageId)
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (instances.containsKey(event.messageIdLong)) {
            listeners[event.messageIdLong]?.forEach {
                if (event.reactionEmote.isEmoji)
                    it.onReactionAdded(event.userIdLong, instances[event.messageIdLong]!!, event.reactionEmote.emoji)
            }
        }
    }

    override fun onMessageReactionRemove(event: MessageReactionRemoveEvent) {
        if (instances.containsKey(event.messageIdLong)) {
            listeners[event.messageIdLong]?.forEach {
                if (event.reactionEmote.isEmoji)
                    it.onReactionRemoved(event.userIdLong, instances[event.messageIdLong]!!, event.reactionEmote.emoji)
            }
        }
    }
}