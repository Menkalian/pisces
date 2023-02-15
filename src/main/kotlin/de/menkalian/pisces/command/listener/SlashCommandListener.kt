package de.menkalian.pisces.command.listener

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.command.ICommand
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.command.data.EParameterType
import de.menkalian.pisces.util.Variables
import de.menkalian.pisces.util.logger
import de.menkalian.pisces.util.shortenTo
import de.menkalian.pisces.variables.FlunderKey
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


/**
 * Listener zuständig für die Verarbeitung der sogenannten "SlashCommands" von Discord.
 * Dieser Listener registriert die gültigen Commands bei Discord, verarbeitet diese bei Ausführung und parst die übergebenen Parameter.
 */
@Suppress("RedundantModalityModifier")
@Service
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.SlashCommandListener"])
class SlashCommandListener(final val commandHandler: ICommandHandler) : ListenerAdapter() {
    private val commandMap: MutableMap<Long, ICommand> = hashMapOf()

    override fun onReady(event: ReadyEvent) {
        val jda = event.jda
        val updateAction = jda.updateCommands()
        val comNameMap = commandHandler.commandsByName
        logger().info("Initializing native Discord commands")

        commandHandler.commands
            .filter {
                it supports ECommandSource.COMMAND
                        && it supports ChannelType.PRIVATE
                        && it supports ChannelType.TEXT
            }
            .forEach {
                logger().debug("Adding \"${it.name}\" as native Discord command")
                val commandData = CommandData(it.name.lowercase().shortenTo(32), it.description.shortenTo(100))

                it.parameters.forEach { param ->
                    val optionType = when (param.type) {
                        EParameterType.BOOLEAN   -> OptionType.BOOLEAN
                        EParameterType.INTEGER   -> OptionType.INTEGER
                        EParameterType.STRING    -> OptionType.STRING
                        EParameterType.USER      -> OptionType.USER
                        EParameterType.TIMESTAMP -> OptionType.STRING
                        EParameterType.DATE      -> OptionType.STRING
                        EParameterType.TIME      -> OptionType.STRING
                    }
                    val name = if (param.name.isEmpty()) "args" else param.name
                    logger().trace("Adding `--$name` as parameter to ${it.name}")
                    commandData.addOption(optionType, name.lowercase().shortenTo(32), param.description.shortenTo(100))
                }

                updateAction.addCommands(commandData)
            }
        updateAction.queue { commands ->
            logger().info("Commands sucessfully updated at Discord")
            commands
                .mapNotNull { comNameMap[it.name]?.let { com -> it.idLong to com } }
                .forEach { commandMap[it.first] = it.second }
        }
    }


    override fun onSlashCommand(event: SlashCommandEvent) {
        val guildId = if (event.isFromGuild) event.guild?.idLong ?: 0L else 0L
        val channelId = if (event.isFromGuild) event.channel.idLong else event.user.idLong
        val command = commandMap[event.commandIdLong]
        logger().info("Processing native Discord command from guild $guildId, channel $channelId: ${event.commandPath}")
        logger().debug("Associated native command with $command")

        if (command != null) {
            val parameters = command.parameters.map { it.copy() }
            parameters.forEach {
                val commandOption =
                    if (it.name.isBlank())
                        event.getOption("args")
                    else
                        event.getOption(it.name)

                val datePattern = "dd.MM.yyyy"
                val timePattern = "HH:mm:ss"
                val dateTimePattern = "$datePattern-$timePattern"

                it.currentValue = when (it.type) {
                    EParameterType.BOOLEAN   -> commandOption?.asBoolean ?: it.asBoolean()
                    EParameterType.INTEGER   -> commandOption?.asLong?.toInt() ?: it.asInt()
                    EParameterType.STRING    -> commandOption?.asString ?: it.asString()
                    EParameterType.USER      -> commandOption?.asMember?.idLong ?: it.asUserId()
                    EParameterType.TIMESTAMP -> commandOption
                        ?.let { option ->
                            LocalDateTime.parse(option.asString, DateTimeFormatter.ofPattern(dateTimePattern))
                        }
                        ?: it.asTimestamp()

                    EParameterType.DATE      -> commandOption
                        ?.let { option ->
                            LocalDate.parse(option.asString, DateTimeFormatter.ofPattern(datePattern))
                        }
                        ?: it.asDate()

                    EParameterType.TIME      -> commandOption
                        ?.let { option ->
                            LocalTime.parse(option.asString, DateTimeFormatter.ofPattern(timePattern))
                        }
                        ?: it.asTime()
                }
            }
            logger().trace("Loaded parameters: $parameters")

            val additionalVars: Variables = hashMapOf()
            additionalVars[FlunderKey.Flunder.Command.User.Name] = event.user.name

            if (event.isFromGuild) {
                additionalVars[FlunderKey.Flunder.Command.Guild.Name] = event.guild?.name ?: ""
                additionalVars[FlunderKey.Flunder.Command.Channel.Name] = event.channel.name
            }

            try {
                command.execute(
                    commandHandler,
                    source = ECommandSource.COMMAND,
                    parameters = parameters,
                    guildId,
                    channelId,
                    event.user.idLong,
                    additionalVars
                )
                event.reply("Befehl erfolgreich").setEphemeral(true).queue()
            } catch (ex: Exception) {
                logger().error("Error when executing native Discord command", ex)
                event.reply("Ein Fehler ist aufgetreten").setEphemeral(true).queue()
            }
        }
    }
}