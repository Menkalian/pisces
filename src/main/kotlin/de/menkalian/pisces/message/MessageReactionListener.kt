package de.menkalian.pisces.message

import de.menkalian.pisces.util.logger
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
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
            logger().info("Reaction added: ${event.reaction.emoji} was added to message ${event.messageIdLong}")
            val doRemove = listeners[event.messageIdLong]?.any {
                event.reaction.emoji is UnicodeEmoji
                        && it.onReactionAdded(event.userIdLong, instances[event.messageIdLong]!!, event.reaction.emoji.asReactionCode)
            } ?: false

            if (doRemove) {
                logger().debug("Removing added reaction, since it was processed successfully")
                try {
                    event.user?.let { event.reaction.removeReaction(it).queue() }
                } catch (ex: Exception) {
                    logger().error("Could not remove reaction", ex)
                }
            }
        }
    }

    override fun onMessageReactionRemove(event: MessageReactionRemoveEvent) {
        if (instances.containsKey(event.messageIdLong)) {
            logger().info("Reaction removed: ${event.reaction.emoji} was removed from message ${event.messageIdLong}")
            listeners[event.messageIdLong]?.forEach {
                if (event.reaction.emoji is UnicodeEmoji)
                    it.onReactionRemoved(event.userIdLong, instances[event.messageIdLong]!!, event.reaction.emoji.asReactionCode)
            }
        }
    }
}