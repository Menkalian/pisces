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
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.Leave"])
class LeaveCommand(override val databaseHandler: IDatabaseHandler, val messageHandler: IMessageHandler, val audioHandler: IAudioHandler) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("l")
        aliases.add("daIstDieTür")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "leave"
    override val description: String
        get() = "Der Bot verlässt den aktuellen Voice-Channel."

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
        val hasDisconnected = controller.disconnect()

        if (hasDisconnected) {
            messageHandler
                .createMessage(guildId, channelId)
                .withTitle("Verbindung vom Voice-Channel erfolgreich getrennt.")
                .build()
        } else {
            messageHandler
                .createMessage(guildId, channelId)
                .withColor(red = 255.toByte(), green = 136.toByte()) // Orange
                .withTitle("Die Verbindung vom Voice-Channel konnte nicht getrennt werden.")
                .build()

        }
    }
}