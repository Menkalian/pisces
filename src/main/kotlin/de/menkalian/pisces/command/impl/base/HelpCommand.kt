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
@RequiresKey(["pisces.command.impl.base.Help"])
class HelpCommand(override val databaseHandler: IDatabaseHandler, val messageHandler: IMessageHandler) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("?")
        aliases.add("h")
        aliases.add("hilfe")

        supportedContexts.addAll(ALL_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "help"
    override val description: String
        get() = "Zeigt eine Liste aller unterstützten Commands mit einer kurzen Beschreibung."

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        val helpMessage = messageHandler
            .createMessage(guildId, channelId)
            .withTitle("Liste der unterstützten Commands")

        commandHandler.commands.forEach {
            helpMessage.addField(it.name, it.description, false)
        }

        helpMessage.build()
    }
}