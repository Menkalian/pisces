package de.menkalian.pisces.command.impl.base

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.FixedVariables
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.base.Usage"])
class UsageCommand(override val databaseHandler: IDatabaseHandler, val messageHandler: IMessageHandler) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("xhelp")
        aliases.add("??")

        supportedContexts.addAll(ALL_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addStringParameter(description = "Name des Befehls, für den erweiterte Informationen ausgegeben werden sollen.")

        super.initialize()
    }

    override val name: String
        get() = "usage"
    override val description: String
        get() = "Erweiterte Informationen zu einem bestimmten Befehl."

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        val commandName = databaseHandler.getFormalCommandName(guildId, parameters.getDefaultArg()?.asString() ?: "")

        val helpMessage = messageHandler
            .createMessage(guildId, channelId)
            .withTitle("Erweiterte Informationen zu *$commandName*")

        val command = commandHandler.commands.firstOrNull {
            it.name == commandName
        }

        if (command != null) {
            helpMessage
                .withText(command.description)

            command.parameters.forEach {
                val title = when {
                    it.name.isBlank()       -> "Zusatzargument (hinter allen anderen Parametern anzugeben)"
                    it.short.isWhitespace() -> "--${it.name}"
                    else                    -> "--${it.name}, -${it.short}"
                }
                helpMessage.addField(title, it.description)
            }
        } else {
            helpMessage
                .withText("Der angefragte Befehl existiert nicht (oder ist im aktuellen Build deaktiviert).")
                .withColor(red = 255.toByte())
        }

        helpMessage.build()
    }
}