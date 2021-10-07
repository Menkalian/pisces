package de.menkalian.pisces.command.impl.base

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.discord.IDiscordHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.FixedVariables
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.base.Ping"])
class PingCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val beanFactory: BeanFactory
) : CommonCommandBase() {
    private lateinit var discordHandler: IDiscordHandler

    override fun initialize() {
        aliases.add("pong")

        supportedContexts.addAll(ALL_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        discordHandler = beanFactory.getBean(IDiscordHandler::class.java)

        super.initialize()
    }

    override val name: String
        get() = "ping"
    override val description: String
        get() = "Einfacher Befehl zum Testen der Verbindung des Bots zur Discord API."

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        messageHandler
            .createMessage(guildId, channelId)
            .withTitle("Ping! Pong!")
            .withText(
                "Verbindungslatenz zum Discord Gateway: ${discordHandler.gatewayPing} ms\n" +
                        "Antwortlatenz der API: ${discordHandler.restPing} ms"
            )
            .build()
    }
}