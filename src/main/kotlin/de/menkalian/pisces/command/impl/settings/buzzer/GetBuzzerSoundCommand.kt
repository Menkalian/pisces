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

/**
 * Implementierung eines Befehls zur Ausgabe der aktuellen Joinsound-Einstellungen
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.settings.buzzer.Get"])
class GetBuzzerSoundCommand(override val databaseHandler: IDatabaseHandler, val messageHandler: IMessageHandler) : CommonCommandBase() {
    override fun initialize() {
        innerCategory = "Tool"

        aliases.add("getbuz")

        supportedContexts.addAll(ALL_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "getBuzzer"
    override val description: String
        get() = "Ermittelt den aktuell eingestellten Buzzersound des Servers und gibt Informationen dazu aus."

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        val currentBuzzersound = databaseHandler.getGuildBuzzersound(guildId)
        val msg = messageHandler
            .createMessage(guildId, channelId)
            .withTitle("Aktueller Buzzersound von %s".format(sourceInformation.get(Flunder.Command.Guild.Name)))

        if (currentBuzzersound != null) {
            msg
                .addField(
                    currentBuzzersound.title,
                    """
                       Url: ${currentBuzzersound.url}
                       Duration: ${currentBuzzersound.duration.toDurationString()}
                    """.trimIndent()
                )

        } else {
            msg
                .withWarningColor()
                .withText("Aktuell ist kein Buzzersound festgelegt".asBold())
        }
        msg.build()
    }
}