package de.menkalian.pisces.command.impl.base

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.Emoji
import de.menkalian.pisces.util.FixedVariables
import de.menkalian.pisces.util.withErrorColor
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

/**
 * Implementierung eines Befehls zum Senden einer Grußnachricht an einen Nutzer
 *
 * Dieser Befehl wurde vor allem zu Testzwecken implementiert und ist üblicherweise in Produktivbuilds deaktiviert.
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.base.Greet"])
class GreetCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler
) : CommonCommandBase() {
    override fun initialize() {
        innerCategory = "Tool"

        supportedContexts.addAll(ALL_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addUserParameter(description = "Account, an den die Grußnachricht gehen soll")

        super.initialize()
    }

    override val name: String
        get() = "hello"
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
        val targetUserId = parameters.getDefaultArg()!!
        if (targetUserId.asUserId() != -1L) {
            messageHandler
                .createPrivateMessage(targetUserId.asUserId())
                .withTitle("Greetings")
                .withText("Jemand möchte dir Grüße schicken ^^. Sag ihm/ihr doch Danke ${Emoji.SLIGHT_SMILE}.")
                .build()
        } else {
            messageHandler
                .createMessage(guildId, channelId)
                .withTitle("Mission failed")
                .withText("Du hast entweder keinen Nutzer angegeben, oder diesem Nutzer konnte keine Nachricht gesendet werden.")
                .withErrorColor()
                .build()
        }
    }
}