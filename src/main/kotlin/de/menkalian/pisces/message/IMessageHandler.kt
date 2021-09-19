package de.menkalian.pisces.message

import de.menkalian.pisces.IHandler

/**
 * Schnittstelle zur Nachrichtenbehandlung.
 */
interface IMessageHandler : IHandler {
    /**
     * Erstellt einen neuen [MessageBuilder] für die angegebenen IDs.
     * Um die Nachricht zu senden, muss die [MessageBuilder.build]-Methode aufgerufen werden.
     *
     * @param guildId Discord-ID des Zielservers
     * @param channelId Discord-ID des Zielchannels
     */
    fun createMessage(guildId: Long, channelId: Long) : MessageBuilder

    /**
     * Erstellt einen neuen [MessageBuilder] für die angegebenen IDs.
     * Um die Nachricht zu senden, muss die [MessageBuilder.build]-Methode aufgerufen werden.
     *
     * @param userId Discord-ID des/der Empfänger*in (User)
     */
    fun createPrivateMessage(userId: Long) : MessageBuilder

    /**
     * Entfernt die angegebene [IMessageInstance] aus der Nachrichtenbehandlung.
     */
    fun invalidateMessage(messageInstance: IMessageInstance)

    /**
     * Entfernt alle [IReactionListener] für die angegebene [IMessageInstance]
     */
    fun clearAllReactionListeners(messageInstance: IMessageInstance)
    /**
     * Fügt einen [IReactionListener] für die angegebene [IMessageInstance] hinzu
     */
    fun addReactionListener(messageInstance: IMessageInstance, listener: IReactionListener)
    /**
     * Entfernt einen [IReactionListener] für die angegebene [IMessageInstance]
     */
    fun removeReactionListener(messageInstance: IMessageInstance, listener: IReactionListener)

    /**
     * Listener zur Behandlung von hinzugefügten/entfernten Nachrichtenreaktionen
     */
    interface IReactionListener {
        /**
         * Wird aufgerufen, wenn eine beliebige Reaktion zu einer Nachricht hinzugefügt wurde.
         *
         * @param userId Discord-ID des/der Nutzer*in, der/die die Reaktion hinzugefügt hat.
         * @param messageInstance Instanz für die das Event aufgetreten ist.
         * @param reaction UTF-8 Repräsentation des Unicode-Emoji, das in der Reaktion enthalten war.
         */
        fun onReactionAdded(userId: Long, messageInstance: IMessageInstance, reaction: String)

        /**
         * Wird aufgerufen, wenn eine beliebige Reaktion von einer Nachricht entfernt wurde.
         *
         * @param userId Discord-ID des/der Nutzer*in, dessen/deren Reaktion entfernt wurde.
         * @param messageInstance Instanz für die das Event aufgetreten ist.
         * @param reaction UTF-8 Repräsentation des Unicode-Emoji, das in der Reaktion enthalten war.
         */
        fun onReactionRemoved(userId: Long, messageInstance: IMessageInstance, reaction: String)
    }
}