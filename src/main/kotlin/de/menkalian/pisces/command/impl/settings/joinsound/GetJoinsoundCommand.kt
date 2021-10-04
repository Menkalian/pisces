package de.menkalian.pisces.command.impl.settings.joinsound

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.FixedVariables
import de.menkalian.pisces.util.asBold
import de.menkalian.pisces.util.toDurationString
import de.menkalian.pisces.util.withWarningColor
import de.menkalian.pisces.variables.FlunderKey.Flunder
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.settings.joinsound.Get"])
class GetJoinsoundCommand(override val databaseHandler: IDatabaseHandler, val messageHandler: IMessageHandler) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("getjs")

        supportedContexts.addAll(ALL_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "getJoinsound"
    override val description: String
        get() = "Ermittelt deinen aktuell eingestellten Joinsound und gibt Informationen dazu aus."

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        val currentJoinsound = databaseHandler.getUserJoinsound(authorId)
        val msg = messageHandler
            .createMessage(guildId, channelId)
            .withTitle("Aktueller Joinsound von %s".format(sourceInformation.get(Flunder.Command.User.Name)))

        if (currentJoinsound != null) {
            msg
                .addField(
                    currentJoinsound.title,
                    """
                       Url: ${currentJoinsound.url}
                       Duration: ${currentJoinsound.duration.toDurationString()}
                    """.trimIndent()
                )

        } else {
            msg
                .withWarningColor()
                .withText("Aktuell ist kein Joinsound festgelegt".asBold())
        }
        msg.build()
    }
}