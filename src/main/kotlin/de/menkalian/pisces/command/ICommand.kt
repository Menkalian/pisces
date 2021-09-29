package de.menkalian.pisces.command

import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.util.FixedVariables
import net.dv8tion.jda.api.entities.ChannelType

/**
 * Schnittstelle eines Befehls.
 * Befehle sind die Schnittstelle zwischen dem Benutzer und den Bots.
 *
 * Über Dependency-Injection werden automatisch alle Kommandos, die als Spring-Bean vorliegen
 */
interface ICommand {
    val name: String
    val description: String
    val parameters: List<CommandParameter>

    infix fun supports(type: ChannelType): Boolean
    infix fun supports(source: ECommandSource): Boolean

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