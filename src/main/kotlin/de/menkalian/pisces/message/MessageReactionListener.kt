package de.menkalian.pisces.message

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Service

/**
 * JDA-[ListenerAdapter], der Reaktionen für Nachrichten bearbeitet.
 * Durch diesen Listener werden nur Reaktionen behandelt, deren Emote in Unicode-Standard enthalten ist (also Discord-Standard Emoji)
 * Reaktionen für Nachrichten, die keinen Listener hier registriert haben, werden ignoriert.
 *
 * @property instances Aktuell behandelte [Message Instanzen][IMessageInstance]
 * @property listeners Aktuell aktive Listener
 */
@Service
class MessageReactionListener : ListenerAdapter() {
    private val instances: MutableMap<Long, IMessageInstance> = mutableMapOf()
    private val listeners: MutableMap<Long, MutableList<IMessageHandler.IReactionListener>> = mutableMapOf()

    /**
     * Fügt einen Listener für die angegebene [IMessageInstance] hinzu.
     *
     * @param instance Zielinstanz für die ein Listener hinzugefügt wird
     * @param listener Listener, der hinzugefügt werden soll
     */
    fun addListener(instance: IMessageInstance, listener: IMessageHandler.IReactionListener) {
        instances[instance.messageId] = instance
        if (listeners.containsKey(instance.messageId).not())
            listeners[instance.messageId] = mutableListOf()
        listeners[instance.messageId]?.add(listener)
    }

    /**
     * Entfernt einen Listener für die angegebene [IMessageInstance].
     *
     * @param instance Zielinstanz für die ein Listener entfernt wird
     * @param listener Listener, der entfernt werden soll
     */
    fun removeListener(instance: IMessageInstance, listener: IMessageHandler.IReactionListener) {
        listeners[instance.messageId]?.remove(listener)
    }

    /**
     * Entfernt alle Listener für die angegebene [IMessageInstance].
     *
     * @param instance Zielinstanz, deren Listener entfernt werden
     */
    fun removeListeners(instance: IMessageInstance) {
        instances.remove(instance.messageId)
        listeners.remove(instance.messageId)
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (instances.containsKey(event.messageIdLong)) {
            val doRemove = listeners[event.messageIdLong]?.any {
                event.reactionEmote.isEmoji
                        && it.onReactionAdded(event.userIdLong, instances[event.messageIdLong]!!, event.reactionEmote.emoji)
            } ?: false

            if (doRemove) {
                event.reaction.removeReaction().queue()
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