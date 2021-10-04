package de.menkalian.pisces.message

import java.time.temporal.TemporalAccessor

/**
 * Darstellung einer Nachrichteninstanz.
 * Diese Instanz kann genutzt werden, um eine Nachricht zu bearbeiten oder Reaktionen auf diese Nachricht zu behandeln.
 * Die Länge einer Nachrichteninstanz ist nicht beschränkt.
 * Falls der Inhalt zu lang für Discord ist, wird automagisch eine Aufteilung in mehrere Seiten vorgenommen und der Wechsel zwischen diesen Seiten behandelt.
 *
 * @property guildId Discord-ID des Servers, auf dem die Nachricht gesendet wurde. Falls dieser Wert `null` ist wurde die Nachricht in einem privaten Channel gesendet.
 * @property channelId Discord-ID des Channels, in dem die Nachricht gesendet wurde. Falls [guildId] `null` ist, ist dieser Wert die User-ID des/der Empfänger*in.
 * @property messageId Discord-ID der Nachricht.
 */
interface IMessageInstance {
    val guildId: Long?
    val channelId: Long
    val messageId: Long

    /**
     * Ändert die Autorenangaben der Nachricht und bearbeitet die Nachricht damit.
     * @see de.menkalian.pisces.message.spec.MessageSpec.withAuthor
     */
    fun setAuthor(name: String? = null, url: String? = null, iconUrl: String? = null): IMessageInstance

    /**
     * Ändert die Titelangabe der Nachricht und bearbeitet die Nachricht damit.
     * @see de.menkalian.pisces.message.spec.MessageSpec.withTitle
     */
    fun setTitle(title: String? = null): IMessageInstance

    /**
     * Ändert den Textinhalt der Nachricht und bearbeitet die Nachricht damit.
     * @see de.menkalian.pisces.message.spec.MessageSpec.withText
     */
    fun setText(text: String? = null): IMessageInstance

    /**
     * Ändert den Textinhalt der Nachricht und bearbeitet die Nachricht damit.
     * @see de.menkalian.pisces.message.spec.MessageSpec.appendText
     */
    fun addText(text: Any? = ""): IMessageInstance

    /**
     * Ändert die Textfelder der Nachricht und bearbeitet die Nachricht damit.
     * @see de.menkalian.pisces.message.spec.MessageSpec.clearFields
     */
    fun clearFields(): IMessageInstance

    /**
     * Ändert die Textfelder der Nachricht und bearbeitet die Nachricht damit.
     * @see de.menkalian.pisces.message.spec.MessageSpec.addBlankField
     */
    fun createBlankField(isInline: Boolean): IMessageInstance

    /**
     * Ändert die Textfelder der Nachricht und bearbeitet die Nachricht damit.
     * @see de.menkalian.pisces.message.spec.MessageSpec.addField
     */
    fun createField(name: String = "", text: String = "", isInline: Boolean = false): IMessageInstance

    /**
     * Ändert die Farbangabe der Nachricht und bearbeitet die Nachricht damit.
     * @see de.menkalian.pisces.message.spec.MessageSpec.withColor
     */
    fun setColor(red: Byte = 0, green: Byte = 0, blue: Byte = 0): IMessageInstance

    /**
     * Ändert den Zeitstempel der Nachricht und bearbeitet die Nachricht damit.
     * @see de.menkalian.pisces.message.spec.MessageSpec.withTimestamp
     */
    fun setTimestamp(timestamp: TemporalAccessor? = null): IMessageInstance

    /**
     * Ändert das Bild der Nachricht und bearbeitet die Nachricht damit.
     * @see de.menkalian.pisces.message.spec.MessageSpec.withImage
     */
    fun setImage(imageUrl: String? = null): IMessageInstance

    /**
     * Ändert das Thumbnail der Nachricht und bearbeitet die Nachricht damit.
     * @see de.menkalian.pisces.message.spec.MessageSpec.withThumbnail
     */
    fun setThumbnail(imageUrl: String? = null): IMessageInstance

    /**
     * Ändert die Footer-Angaben der Nachricht und bearbeitet die Nachricht damit.
     * @see de.menkalian.pisces.message.spec.MessageSpec.withFooter
     */
    fun setFooter(text: String? = null, url: String? = null): IMessageInstance

    /**
     * Stoppt den Timeout, der diese Nachricht nach 30 min als ungültig markiert.
     * Wenn dieser Timer gestoppt ist, wird diese Nachricht möglicherweise auch nicht aus dem Reaktionsbehandlungssystem entfernt, daher muss dies gegebenenfalls manuell erfolgen.
     */
    fun stopInvalidationTimer()

    /**
     * Fügt das angegebene Unicode-Emoji als Reaktion an die Nachricht hinzu
     */
    fun addReaction(reaction: String)

    /**
     * Entfernt alle Nutzerreaktionen des angegebenen Unicode-Emojis
     */
    fun clearUserReactions(reaction: String)

    /**
     * Entfernt alle Reaktionen des angegebenen Unicode-Emojis
     */
    fun removeReaction(reaction: String)

    /**
     * Entfernt alle Reaktionen von dieser Nachricht
     */
    fun removeAllReactions()

    /**
     * Fügt einen [IMessageHandler.IReactionListener] für diese Nachricht hinzu.
     * @see IMessageHandler.addReactionListener
     */
    fun addReactionHandler(handler: IMessageHandler.IReactionListener)

    /**
     * Entfernt einen [IMessageHandler.IReactionListener] für diese Nachricht.
     * @see IMessageHandler.removeReactionListener
     */
    fun removeReactionHandler(handler: IMessageHandler.IReactionListener)

    /**
     * Fügt eine Aktion hinzu, die ausgeführt werden soll, wenn dieser Nachricht ein bestimmtes Emoji hinzugefügt wird.
     */
    fun addOnReactionAddedAction(reaction: String, action: IReactionAction)

    /**
     * Fügt eine Aktion hinzu, die ausgeführt werden soll, wenn von dieser Nachricht ein bestimmtes Emoji entfernt wurde.
     */
    fun addOnReactionRemovedAction(reaction: String, action: IReactionAction)

    /**
     * Entfernt alle Aktionen, die für das angegebene Unicode-Emoji gesetzt wurden.
     */
    fun clearReactionActions(reaction: String)

    /**
     * Aktions-Schnittstelle für die Behandlung von Emoji-Events.
     * Diese Aktionen werden bereits für ein bestimmtes Event und eine bestimmte Aktion gesetzt, daher werden diese zwei Informationen nicht mehr in der [onAction]-Methode übergeben.
     */
    fun interface IReactionAction {
        /**
         * Aktion, die ausgelöst wird, wenn das Ereignis eintritt, für das diese [IReactionAction] registriert wurde.
         *
         * @param userId User-ID des/der Nutzer*in, deren Reaktion für das Auslösen des Events verantwortlich war.
         * @param messageInstance Nachrichteninstanz für die das Event aufgetreten ist.
         *
         * @return Ob die Reaktion nach der Verarbeitung wieder entfernt werden soll (falls diese hinzugefügt wurde)
         */
        fun onAction(userId: Long, messageInstance: IMessageInstance): Boolean
    }
}