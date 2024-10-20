package de.menkalian.pisces.command

import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandChannelContext
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.command.data.EParameterType
import de.menkalian.pisces.database.IDatabaseHandler
import net.dv8tion.jda.api.entities.channel.ChannelType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Grundlegende Implementierung für alle Befehle.
 * Diese Implementierung ist von [ICommand] abgetrennt, um die lesbare Schnittstelle (und die öffentlichen Member) möglichst minimal zu halten.
 */
abstract class CommonCommandBase : ICommand {
    protected abstract val databaseHandler: IDatabaseHandler
    override val parameters: MutableList<CommandParameter> = mutableListOf()
    protected open val aliases: MutableList<String> = mutableListOf()
    protected open val supportedContexts: MutableList<ECommandChannelContext> = mutableListOf()
    protected open val supportedSources: MutableList<ECommandSource> = mutableListOf()

    override val category: String
        get() = innerCategory
    protected var innerCategory: String = "Standard"

    companion object {
        val ALL_CONTEXTS = ECommandChannelContext.values().toList()
        val ALL_GUILD_CONTEXTS = listOf(
            ECommandChannelContext.GUILD_ALL,
            ECommandChannelContext.GUILD_STORE,
            ECommandChannelContext.GUILD_TEXT
        )
        val ALL_PRIVATE_CONTEXTS = listOf(
            ECommandChannelContext.PRIVATE
        )

        val ALL_SOURCES = ECommandSource.values()
    }

    internal open fun initialize() {
        aliases.forEach {
            databaseHandler.addCommandShortcut(0L, it.lowercase(), name.lowercase())
        }
    }

    override infix fun supports(type: ChannelType) =
        supportedContexts.any { it.supports(type) }

    override infix fun supports(source: ECommandSource) =
        supportedSources.contains(source)

    /**
     * Fügt einen Parameter des Typs [EParameterType.BOOLEAN] hinzu.
     * Der Standardwert ist hier **immer** `false`
     */
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

    /**
     * Fügt einen Parameter des Typs [EParameterType.INTEGER] hinzu.
     */
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

    /**
     * Fügt einen Parameter des Typs [EParameterType.STRING] hinzu.
     */
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

    /**
     * Fügt einen Parameter des Typs [EParameterType.USER] hinzu.
     * Der Standardwert ist hier **immer** `-1L` (ungültige User-ID)
     */
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

    /**
     * Fügt einen Parameter des Typs [EParameterType.TIMESTAMP] hinzu.
     */
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

    /**
     * Fügt einen Parameter des Typs [EParameterType.TIME] hinzu.
     */
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

    /**
     * Fügt einen Parameter des Typs [EParameterType.DATE] hinzu.
     */
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

    /**
     * Liest das Standardargument ([CommandParameter.name] ist leer) aus einer Parameterliste.
     */
    protected fun List<CommandParameter>.getDefaultArg() = firstOrNull { it.name.isBlank() }
}