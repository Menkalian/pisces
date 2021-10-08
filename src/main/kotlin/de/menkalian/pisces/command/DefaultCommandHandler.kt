package de.menkalian.pisces.command

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.util.CommonHandlerImpl
import de.menkalian.pisces.util.logger
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

/**
 * Standardimplementierung der [ICommandHandler]-Schnittstelle.
 */
@Suppress("RedundantModalityModifier")
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.Handler.DefaultCommandHandler"])
class DefaultCommandHandler(
    final override val commands: List<CommonCommandBase>,
    val databaseHandler: IDatabaseHandler
) : ICommandHandler,
    CommonHandlerImpl() {
    override val commandsByName: Map<String, CommonCommandBase> = commands.associateBy { it.name.lowercase() }

    override fun initialize() {
        logger().info("Starting initialization for ${commands.size} commands")
        commands.forEach {
            logger().info(
                """
                    Found command: ${it.name}
                    Description: ${it.description}
                    Parameters:
                    ${it.parameters.joinToString("\n") { param -> param.toLogString() }.prependIndent("    ")}
                """.trimIndent()

            )
            it.initialize()
        }
        finishInitialization()
    }

    private fun CommandParameter.toLogString(): String {
        val displayName = when {
            name.isBlank()       -> ""
            short.isWhitespace() -> "--${name}"
            else                 -> "--${name}, -${short}"
        }
        return "$displayName (default: \"$defaultValue\") -> \"$description\""
    }

    override fun deinitialize() {
        startDeinitialization()
    }

    override fun getCommand(name: String, guildId: Long): ICommand? {
        val origName = databaseHandler.getFormalCommandName(guildId, name).lowercase()
        logger().debug("Lookup command \"$name\" (=\"$origName\") for $guildId")
        return commandsByName[origName]
    }

    private val prefixChangedListeners = mutableListOf<ICommandHandler.IGuildPrefixChangedListener>()
    override fun fireGuildPrefixChanged(guildId: Long, newPrefix: String) {
        logger().info("Fire prefix changed: \"$newPrefix\" for $guildId")
        prefixChangedListeners
            .parallelStream()
            .forEach { it.onPrefixChanged(guildId, newPrefix) }
    }

    override fun addGuildPrefixChangedListener(listener: ICommandHandler.IGuildPrefixChangedListener) {
        prefixChangedListeners.add(listener)
    }

    override fun removeGuildPrefixChangedListener(listener: ICommandHandler.IGuildPrefixChangedListener) {
        prefixChangedListeners.remove(listener)
    }
}