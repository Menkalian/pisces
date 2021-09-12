package de.menkalian.pisces.command

import de.menkalian.pisces.util.FixedVariables
import net.dv8tion.jda.api.entities.ChannelType

/**
 * Schnittstelle eines Befehls.
 * Befehle sind die Schnittstelle zwischen dem Benutzer und den Bots.
 *
 * Über Dependency-Injection werden automatisch alle Kommandos, die als Spring-Bean vorliegen
 */
interface ICommand {
    enum class ECommandChannelContext {
        PRIVATE, GUILD_ALL, GUILD_TEXT, GUILD_STORE;

        fun supports(type: ChannelType) =
            type.isMessage && when (this) {
                PRIVATE     -> type == ChannelType.PRIVATE
                GUILD_ALL   -> type.isGuild
                GUILD_TEXT  -> type == ChannelType.TEXT
                GUILD_STORE -> type == ChannelType.STORE
            }
    }

    enum class ECommandSource { TEXT, COMMAND }
    enum class ParameterType { INTEGER, STRING, USER, DATE, TIME }

    data class CommandParameter(
        val name: String, val short: Char,
        val description: String,
        val type: ParameterType,
        val defaultValue: Any, val currentValue: Any = defaultValue
    )

    val name: String
    val aliases: List<String>
    val description: String
    val argumentParameter: CommandParameter
    val parameters: List<CommandParameter>

    val supportedContexts: List<ECommandChannelContext>
    infix fun supports(type: ChannelType) =
        supportedContexts.any { it.supports(type) }

    val supportedSources: List<ECommandSource>
    infix fun supports(source: ECommandSource) =
        supportedSources.contains(source)

    fun execute(source: ECommandSource, parameters: List<CommandParameter>, sourceInformation: FixedVariables)
}