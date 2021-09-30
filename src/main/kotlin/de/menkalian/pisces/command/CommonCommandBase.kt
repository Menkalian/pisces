package de.menkalian.pisces.command

import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandChannelContext
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.command.data.EParameterType
import de.menkalian.pisces.database.IDatabaseHandler
import net.dv8tion.jda.api.entities.ChannelType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

abstract class CommonCommandBase : ICommand {
    protected abstract val databaseHandler: IDatabaseHandler
    override val parameters: MutableList<CommandParameter> = mutableListOf()
    protected open val aliases: MutableList<String> = mutableListOf()
    protected open val supportedContexts: MutableList<ECommandChannelContext> = mutableListOf()
    protected open val supportedSources: MutableList<ECommandSource> = mutableListOf()

    companion object {
        val ALL_CONTEXTS = ECommandChannelContext.values().toList()
        val ALL_GUILD_CONTEXTS = listOf<ECommandChannelContext>(
            ECommandChannelContext.GUILD_ALL,
            ECommandChannelContext.GUILD_STORE,
            ECommandChannelContext.GUILD_TEXT
        )
        val ALL_PRIVATE_CONTEXTS = listOf<ECommandChannelContext>(
            ECommandChannelContext.PRIVATE
        )

        val ALL_SOURCES = ECommandSource.values()
    }

    internal open fun initialize() {
        aliases.forEach {
            databaseHandler.addCommandShortcut(0L, it, name)
        }
    }

    override infix fun supports(type: ChannelType) =
        supportedContexts.any { it.supports(type) }

    override infix fun supports(source: ECommandSource) =
        supportedSources.contains(source)

    protected fun addBooleanParameter(
        name: String = "",
        short: Char = ' ',
        description: String = "..."
    ) {
        parameters.add(
            CommandParameter(
                name,
                short,
                description,
                EParameterType.BOOLEAN,
                false
            )
        )
    }

    protected fun addIntParameter(
        name: String = "",
        short: Char = ' ',
        description: String = "...",
        defaultValue: Int = -1
    ) {
        parameters.add(
            CommandParameter(
                name,
                short,
                description,
                EParameterType.INTEGER,
                defaultValue
            )
        )
    }

    protected fun addStringParameter(
        name: String = "",
        short: Char = ' ',
        description: String = "...",
        defaultValue: String = ""
    ) {
        parameters.add(
            CommandParameter(
                name,
                short,
                description,
                EParameterType.STRING,
                defaultValue
            )
        )
    }

    protected fun addUserParameter(
        name: String = "",
        short: Char = ' ',
        description: String = "..."
    ) {
        parameters.add(
            CommandParameter(
                name,
                short,
                description,
                EParameterType.USER,
                -1L
            )
        )
    }

    protected fun addTimestampParameter(
        name: String = "",
        short: Char = ' ',
        description: String = "...",
        defaultValue: LocalDateTime = LocalDateTime.MIN
    ) {
        parameters.add(
            CommandParameter(
                name,
                short,
                description,
                EParameterType.TIMESTAMP,
                defaultValue
            )
        )
    }

    protected fun addTimeParameter(
        name: String = "",
        short: Char = ' ',
        description: String = "...",
        defaultValue: LocalTime = LocalTime.MIN
    ) {
        parameters.add(
            CommandParameter(
                name,
                short,
                description,
                EParameterType.TIME,
                defaultValue
            )
        )
    }

    protected fun addDateParameter(
        name: String = "",
        short: Char = ' ',
        description: String = "...",
        defaultValue: LocalDate = LocalDate.MIN
    ) {
        parameters.add(
            CommandParameter(
                name,
                short,
                description,
                EParameterType.DATE,
                defaultValue
            )
        )
    }

    protected fun List<CommandParameter>.getDefaultArg() = firstOrNull { it.name.isBlank() }
}