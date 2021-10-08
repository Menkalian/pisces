package de.menkalian.pisces.command

import de.menkalian.pisces.IHandler

/**
 * Der [ICommandHandler] ist eine Schnittstelle für die Verwaltung von Befehlen.
 * Die Aufgaben dieses Handlers sind die Initialisierung und Speicherung der Befehlsobjekte, sowie die Benachrichtigung anderer Komponenten, wenn das Präfix für einen Server geändert wurde.
 */
interface ICommandHandler : IHandler {
    /**
     * Eine Liste aller Befehlsobjekte
     */
    val commands: List<ICommand>

    /**
     * Alle Befehle, sortiert nach [ihrem Namen][ICommand.name]
     */
    val commandsByName: Map<String, ICommand>

    /**
     * Abfrage des zugehörigen [ICommand]-Objekts für die übergebenen Parameter
     *
     * @param name Name oder Alias des Befehls
     * @param guildId ID des Servers, für den das Alias nachgeschaut werden soll
     * @return Das passende Objekt oder `null` falls es kein Befehl für das angegebene Alias gibt
     */
    fun getCommand(name: String, guildId: Long = 0L): ICommand?

    /**
     * Verteilt das Event im System, dass für einen Server das Präfix geändert wurde.
     *
     * @param guildId ID des Servers, für den das Präfix angepasst wurde
     * @param newPrefix neues Präfix des Servers
     */
    fun fireGuildPrefixChanged(guildId: Long, newPrefix: String)

    /**
     * Registriert einen neuen Listener, der benachrichtigt wird, wenn das Präfix für einen Server geändert worden ist
     *
     * @param listener [IGuildPrefixChangedListener]-Objekt, das registriert werden soll.
     */
    fun addGuildPrefixChangedListener(listener: IGuildPrefixChangedListener)

    /**
     * Entfernt einen Listener, der (nicht mehr) benachrichtigt werden soll, wenn das Präfix für einen Server geändert worden ist
     *
     * @param listener [IGuildPrefixChangedListener]-Objekt, das entfernt werden soll.
     */
    fun removeGuildPrefixChangedListener(listener: IGuildPrefixChangedListener)

    /**
     * Definition eines Listeners, der benachrichtigt werden soll, wenn das Präfix für einen Server geändert werden soll.
     */
    fun interface IGuildPrefixChangedListener {
        /**
         * Diese Methode wird aufgerufen, wenn das Präfix für einen Server geändert wird.
         *
         * @param guildId Discord-ID des Servers für den die ID geändert wurde
         * @param newPrefix neues Präfix für den Server
         */
        fun onPrefixChanged(guildId: Long, newPrefix: String)
    }

}