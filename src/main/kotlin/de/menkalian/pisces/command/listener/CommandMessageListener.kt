package de.menkalian.pisces.command.listener

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.command.ICommand
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.command.data.EParameterType
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.Variables
import de.menkalian.pisces.util.isEven
import de.menkalian.pisces.util.logger
import de.menkalian.pisces.util.withErrorColor
import de.menkalian.pisces.variables.FlunderKey.Flunder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


/**
 * Standardbehandlung für alle Nachrichten, die per Textnachricht gesendet werden.
 * Diese Nachrichten sind folgendermaßen aufgebaut:
 *      `{PREFIX}{COMMANDALIAS} [PARAMETERS] [DEFAULT_ARGUMENT]`
 *
 * Der Listener prüft alle Nachrichten auf Relevanz und parst gegebenenfalls die Parameter.
 */
@Suppress("RedundantModalityModifier")
@Service
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.CommandMessageListener"])
class CommandMessageListener(
    final val commandHandler: ICommandHandler,
    val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler
) : ListenerAdapter() {

    companion object {
        /**
         * Standardprefix zur Verwendung falls es Probleme bei der Datenbankabfrage gibt.
         */
        private const val DEFAULT_PREFIX = "!"
    }

    /**
     * Map zum Cachen der Präfixe für die einzelnen Guilds/Server.
     * Die Präfixe werden gecached um die Latenz pro Befehlsausführung zu verringern.
     * Wird ein Präfix während der Laufzeit geändert, so wird dies an [ICommandHandler.IGuildPrefixChangedListener] bekannt gegeben.
     */
    private val guildPrefixMap: MutableMap<Long, String> = hashMapOf()

    init {
        commandHandler.addGuildPrefixChangedListener { id, prefix ->
            guildPrefixMap[id] = prefix
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val guildId = if (event.isFromGuild) event.guild.idLong else 0L
        val channelId = if (event.isFromGuild) event.channel.idLong else event.author.idLong

        val prefix = getPrefix(guildId)
        val msg = event.message.contentRaw
        if (msg.startsWith(prefix)) {
            logger().trace("Prefix \"$prefix\" recognized in \"$msg\"")
            val fullCommandString = msg.substring(prefix.length)
            logger().info("Received command: \"$fullCommandString\"")
            val resolvedCommand = commandHandler.getCommand(fullCommandString.parseCommand(), guildId)

            if (resolvedCommand != null
                && resolvedCommand supports event.channelType
                && resolvedCommand supports ECommandSource.TEXT
            ) {
                logger().info("Executing command $resolvedCommand")
                val additionalVars: Variables = hashMapOf()
                additionalVars[Flunder.Command.User.Name] = event.author.name

                if (event.isFromGuild) {
                    additionalVars[Flunder.Command.Guild.Name] = event.guild.name
                    additionalVars[Flunder.Command.Channel.Name] = event.channel.name
                }

                val parameters =
                    parseParameters(fullCommandString.parseParameterString(), resolvedCommand)

                resolvedCommand.execute(
                    commandHandler,
                    ECommandSource.TEXT,
                    parameters,
                    guildId, channelId,
                    event.author.idLong,
                    additionalVars
                )
            } else {
                logger().warn("Command $resolvedCommand could not handle a command from Guild $guildId, Channel $channelId")
                notifyInvalidCommand(guildId, channelId)
            }
        }
    }

    private fun parseParameters(parameterString: String, command: ICommand): List<CommandParameter> {
        logger().info("Parsing parameters from $parameterString")

        @Suppress("RegExpRedundantEscape")
        val sectioned = parameterString
            // Split by all non-escaped quotations
            .split("(?<!\\\\)\\\"".toRegex())
            // Unescape remaining quotations
            .map { it.replace("\\\"", "\"") }
            // even index -> unescaped -> split by whitespaces
            // else -> escaped -> use as raw string
            .flatMapIndexed { index, str ->
                if (index.isEven()) {
                    str.split("\\s+".toRegex())
                } else {
                    listOf(str)
                }
            }
        logger().trace("Built sections: $sectioned")

        val parsedParameters = command.parameters.map { it.copy() }
        var highestIndex = -1

        // Parse all the parameters except the default one
        parsedParameters.forEach { param ->
            if (param.name.isNotBlank()) {
                var paramIndex = sectioned.indexOf("--" + param.name)
                if (paramIndex == -1 && !param.short.isWhitespace()) {
                    paramIndex = sectioned.indexOf("-" + param.short)
                }

                if (paramIndex != -1) {
                    logger().debug("Found ${param.name} at index $paramIndex")
                    if (param.type != EParameterType.BOOLEAN) {
                        try {
                            highestIndex = maxOf(highestIndex, paramIndex + 1)
                            param.currentValue = parseParameter(sectioned.getOrNull(paramIndex + 1), param.type)
                        } catch (ex: Exception) {
                            logger().error("Error when parsing parameter", ex)
                            param.currentValue = param.defaultValue
                        }
                    } else {
                        // Just the apperance of the parameter is enough to set the value
                        highestIndex = maxOf(highestIndex, paramIndex)
                        param.currentValue = true
                    }
                } else {
                    logger().debug("Did not find parameter ${param.name}")
                }
            }
        }

        // Use the remaining string as default (nameless) argument/parameter
        parsedParameters
            .firstOrNull { it.name.isBlank() }
            ?.let { param ->
                if (sectioned.size > highestIndex + 1) {
                    val parameterValue = sectioned
                        .subList(highestIndex + 1, sectioned.size)
                        .joinToString(" ")

                    logger().debug("Treating \"$parameterValue\" as remaining argument")
                    try {
                        if (parameterValue.isNotBlank()) {
                            param.currentValue = parseParameter(parameterValue, param.type)
                        }
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

    private fun notifyInvalidCommand(guildId: Long = 0L, channelId: Long) {
        val sentMessage = messageHandler.createMessage(guildId, channelId)
            .withTitle("Ungültiges Command")
            .withText("Das Command wurde nicht erkannt und konnte daher nicht ausgeführt werden.")
            .withErrorColor()
            .build()
        messageHandler.clearAllReactionListeners(sentMessage)
        sentMessage.stopInvalidationTimer()
    }

    private fun getPrefix(guildId: Long): String {
        if (!guildPrefixMap.containsKey(guildId)) {
            loadGuild(guildId)
            logger().debug("Caching prefix for guild $guildId")
        }

        return guildPrefixMap[guildId] ?: DEFAULT_PREFIX
    }

    private fun loadGuild(id: Long) {
        guildPrefixMap[id] = databaseHandler.getSettingsValue(id, Flunder.Guild.Settings.Prefix.toString(), DEFAULT_PREFIX)
    }

    private fun String.parseCommand(): String {
        return split("\\s+".toRegex(), limit = 2)[0]
    }

    private fun String.parseParameterString(): String {
        return split("\\s+".toRegex(), limit = 2).getOrNull(1) ?: ""
    }
}