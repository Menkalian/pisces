package de.menkalian.pisces.command.impl.audio

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.audio.IAudioHandler
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.FixedVariables
import de.menkalian.pisces.util.withWarningColor
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.Join"])
class JoinCommand(override val databaseHandler: IDatabaseHandler, val messageHandler: IMessageHandler, val audioHandler: IAudioHandler) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("j")
        aliases.add("kommmalher")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "join"
    override val description: String
        get() = "Der Bot tritt deinem aktuellen Voice-Channel bei."

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        val controller = audioHandler.getGuildAudioController(guildId)
        val targetId = controller.getUserVoiceChannelId(authorId)

        if (targetId != null && controller.connect(targetId)) {
            messageHandler
                .createMessage(guildId, channelId)
                .withTitle("Verbindung zu deinem Voice-Channel erfolgreich hergestellt.")
                .build()
        } else {
            messageHandler
                .createMessage(guildId, channelId)
                .withWarningColor()
                .withTitle(
                    if (targetId != null) {
                        "Du bist in keinem Voice-Channel. Der Bot kann dir also nicht beitreten."
                    } else {
                        "Die Verbindung zum Voice-Channel ist fehlgeschlagen. Möglicherweise fehlen dem Bot die nötigen Rechte."
                    }
                )
                .build()

        }
    }
}