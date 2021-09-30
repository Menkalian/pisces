package de.menkalian.pisces.command

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.command.data.EParameterType
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.CommonHandlerImpl
import de.menkalian.pisces.util.Variables
import de.menkalian.pisces.util.isEven
import de.menkalian.pisces.util.logger
import net.dv8tion.jda.api.entities.ChannelType
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Suppress("RedundantModalityModifier")
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.Handler.DefaultCommandHandler"])
class DefaultCommandHandler(
    final override val commands: List<CommonCommandBase>,
    val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler
) : ICommandHandler,
    CommonHandlerImpl() {
    private val commandMap: Map<String, CommonCommandBase> = commands.associateBy { it.name }

    override fun initialize() {
        commands.forEach { it.initialize() }
        finishInitialization()
    }

    override fun deinitialize() {
        startDeinitialization()
    }

    override fun executeGuildCommand(
        cmdString: String,
        guildId: Long,
        source: ECommandSource,
        channelId: Long,
        channelType: ChannelType,
        authorId: Long,
        additionalVars: Variables
    ) {
        val commandName = getCommandName(cmdString, guildId)
        val command = commandMap[commandName]

        if (command != null
            && command supports channelType
            && command supports source
        ) {
            val parameters = parseParameters(getParameterString(cmdString), command)
            command.execute(
                this, source,
                parameters,
                guildId, channelId, authorId,
                additionalVars
            )
        } else {
            notifyInvalidCommand(guildId, channelId)
        }
    }

    override fun executePrivateCommand(cmdString: String, authorId: Long, additionalVars: Variables) {
        val commandName = getCommandName(cmdString, 0L)
        val command = commandMap[commandName]

        if (command != null
            && command supports ChannelType.PRIVATE
            && command supports ECommandSource.TEXT
        ) {
            val parameters = parseParameters(getParameterString(cmdString), command)
            command.execute(
                this, ECommandSource.TEXT,
                parameters,
                0L, authorId, authorId,
                additionalVars
            )
        } else {
            notifyInvalidCommand(channelId = authorId)
        }
    }

    private fun notifyInvalidCommand(guildId: Long = 0L, channelId: Long) {
        val sentMessage = messageHandler.createMessage(guildId, channelId)
            .withTitle("Ungültiges Command")
            .withText("Das Command wurde nicht erkannt und konnte daher nicht ausgeführt werden.")
            .withColor(red = 0xff.toByte())
            .build()
        messageHandler.clearAllReactionListeners(sentMessage)
        sentMessage.stopInvalidationTimer()
    }

    private fun getCommandName(commandString: String, guildId: Long = 0L): String {
        val writtenName = commandString.split(" ", limit = 2)[0]
        return databaseHandler.getFormalCommandName(guildId, writtenName)
    }

    private fun getParameterString(commandString: String): String {
        return commandString.split(" ", limit = 2).getOrNull(1) ?: ""
    }

    private fun parseParameters(commandString: String, command: CommonCommandBase): List<CommandParameter> {
        @Suppress("RegExpRedundantEscape")
        val sectioned = commandString
            .split("(?<!\\\\)\\\"".toRegex()) // Split by all non-escaped quotations
            .map { it.replace("\\\"", "\"") }
            .flatMapIndexed { index, str ->
                if (index.isEven()) {
                    str.split("\\s+".toRegex())
                } else {
                    listOf(str)
                }
            }

        val parsedParameters = command.parameters.map { it.copy() }
        var highestIndex = -1
        parsedParameters.forEach { param ->
            if (param.name.isNotBlank()) {
                var paramIndex = sectioned.indexOf("--" + param.name)
                if (paramIndex == -1 && !param.short.isWhitespace()) {
                    paramIndex = sectioned.indexOf("-" + param.short)
                }

                if (paramIndex != -1) {
                    if (param.type == EParameterType.BOOLEAN) {
                        highestIndex = maxOf(highestIndex, paramIndex)
                        param.currentValue = true
                    } else {
                        try {
                            highestIndex = maxOf(highestIndex, paramIndex + 1)
                            param.currentValue = parseParameter(sectioned.getOrNull(paramIndex + 1), param.type)
                        } catch (ex: Exception) {
                            logger().error("Error when parsing parameter", ex)
                            param.currentValue = param.defaultValue
                        }
                    }
                }
            }
        }

        parsedParameters
            .firstOrNull { it.name.isBlank() }
            ?.let { param ->
                if (sectioned.size > highestIndex + 1) {
                    val parameterValue = sectioned
                        .subList(highestIndex + 1, sectioned.size)
                        .joinToString(" ")

                    try {
                        param.currentValue = parseParameter(parameterValue, param.type)
                    } catch (ex: Exception) {
                        logger().error("Error when parsing parameter", ex)
                        param.currentValue = param.defaultValue
                    }
                }
            }
        return parsedParameters
    }

    private fun parseParameter(value: String?, type: EParameterType): Any {
        logger().debug("Parsing \"$value\" as $type")

        if (value == null)
            throw IllegalArgumentException("Null value can not be parsed")

        val datePattern = "dd.MM.yyyy"
        val timePattern = "HH:mm:ss"
        val userIdRegex = "<@!(\\d+)>".toRegex().toPattern()
        return when (type) {
            EParameterType.INTEGER   -> value.toIntOrNull() ?: throw IllegalArgumentException("Integer could not be extracted")
            EParameterType.STRING    -> value.toString()
            EParameterType.USER      -> {
                val matcher = userIdRegex.matcher(value)
                if (matcher.matches()) {
                    val userId = matcher.group(1)
                    userId.toLongOrNull() ?: throw IllegalArgumentException("Could not parse UserId")
                } else {
                    throw IllegalArgumentException("Could not parse UserId")
                }
            }
            EParameterType.TIMESTAMP -> LocalDateTime.parse(value, DateTimeFormatter.ofPattern("$datePattern-$timePattern"))
            EParameterType.DATE      -> LocalDate.parse(value, DateTimeFormatter.ofPattern(datePattern))
            EParameterType.TIME      -> LocalTime.parse(value, DateTimeFormatter.ofPattern(timePattern))
            EParameterType.BOOLEAN   -> true
        }
    }

    private val prefixChangedListeners = mutableListOf<ICommandHandler.IGuildPrefixChangedListener>()
    override fun fireGuildPrefixChanged(guildId: Long, newPrefix: String) {
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