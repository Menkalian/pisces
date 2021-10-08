package de.menkalian.pisces.command

import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.util.FixedVariables
import net.dv8tion.jda.api.entities.ChannelType

/**
 * Schnittstelle zur Darstellung eines Befehls.
 * Befehle sind der Interaktionspunkt zwischen den Nutzern und dem Bot.
 *
 * Alle Realisierungen dieser Schnittstelle sollten als Spring-Bean in der Applikation zur Verfügung gestellt werden.
 */
interface ICommand {
    /**
     * Name des Befehls.
     * Über diesen Namen kann der Befehl in jedem Fall aufgerufen werden.
     */
    val name: String

    /**
     * Beschreibung des Befehls, die dem Nutzer angezeigt werden soll
     */
    val description: String

    /**
     * Zusätzliche Parameter, die vom Nutzer angegeben werden können zur Steuerung des Befehls
     */
    val parameters: List<CommandParameter>

    /**
     * Name der Kategorie unter der dieser Befehl in der Hilfe angezeigt werden soll
     */
    val category: String

    /**
     * Prüft ob dieser Befehl in dem angegebenen ChannelTyp ausgeführt werden darf.
     *
     * @param type Typ des Channels, der geprüft werden soll.
     */
    infix fun supports(type: ChannelType): Boolean

    /**
     * Prüft ob dieser Befehl in dem angegebenen ChannelTyp ausgeführt werden darf.
     *
     * @param source Quelle, aus der der Befehl stammt.
     */
    infix fun supports(source: ECommandSource): Boolean

    /**
     * Führt diesen Befehl aus.
     *
     * @param commandHandler Aktive Instanz der [ICommandHandler]-Schnittstelle
     * @param source Quelle, aus der der Befehl stammt.
     * @param parameters Liste der Zusatzparameter mit den aktiven Werten
     * @param guildId ID des Servers, auf dem der Befehl ausgeführt wurde (`0` für private Chats)
     * @param channelId ID des Channels, in dem der Befehl gesendet wurde (für private Chats ist dies die UserID)
     * @param authorId UserID des Nutzers, der den Befehl ausgeführt hat
     * @param sourceInformation Zusatzinformationen, die über Schlüssel-Wert-Paare dargestellt werden.
     */
    fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long = 0L,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables = hashMapOf()
    )
}