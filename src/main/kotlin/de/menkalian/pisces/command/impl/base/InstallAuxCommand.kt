package de.menkalian.pisces.command.impl.base

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.audio.IAudioHandler
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.discord.IDiscordHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.FixedVariables
import de.menkalian.pisces.util.asInlineCode
import de.menkalian.pisces.util.withSuccessColor
import de.menkalian.pisces.util.withWarningColor
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

/**
 * Implementierung eines Befehls der lediglich für einen Witz existiert
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.base.InstallAux"])
class InstallAuxCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler,
    val beanFactory: BeanFactory
) : CommonCommandBase() {
    private lateinit var discordHandler: IDiscordHandler

    override fun initialize() {
        innerCategory = "Tool"

        aliases.add("banmeitho")
        aliases.add("handtuch")

        supportedContexts.addAll(ALL_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        discordHandler = beanFactory.getBean(IDiscordHandler::class.java)

        super.initialize()
    }

    override val name: String
        get() = "installaux"
    override val description: String
        get() = "Ernsthaftes Command ohne einen dummen Dev-Witz dahinter"

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        try {
            discordHandler.installAux()
            messageHandler
                .createMessage(guildId, channelId)
                .withTitle("Erfolgreich Aux-Kabel installiert")
                .withSuccessColor()
                .build()
        } catch (ex: Exception) {
            messageHandler
                .createMessage(guildId, channelId)
                .withTitle("Aktion fehlgeschlagen. Eine Flunder kann kein Aux-Kabel bedienen.")
                .withText("Fehlermeldung: ${ex.message?.asInlineCode()}")
                .withWarningColor()
                .build()
        }
    }
}