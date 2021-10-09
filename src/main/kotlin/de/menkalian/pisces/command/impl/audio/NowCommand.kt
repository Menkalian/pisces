package de.menkalian.pisces.command.impl.audio

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.audio.IAudioHandler
import de.menkalian.pisces.audio.data.AudioSourceType
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.FixedVariables
import de.menkalian.pisces.util.addTrackInfoField
import de.menkalian.pisces.util.withWarningColor
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

/**
 * Implementierung eines Befehls der Informationen zum aktuellen Song zurückgibt
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.Now"])
class NowCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        innerCategory = "Audio"

        aliases.add("n")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "now"
    override val description: String
        get() = "Zeigt Informationen zum aktuell spielenden Track"

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

        controller.getCurrentTrackInfo()?.let {
            val msg = messageHandler
                .createMessage(guildId, channelId)
                .withTitle("Aktueller Track in der Wiedergabe.")
                .addTrackInfoField(it, true)

            if (it.sourcetype == AudioSourceType.YOUTUBE) {
                msg.withThumbnail("https://img.youtube.com/vi/${it.sourceIdentifier}/default.jpg")
            }

            msg.build()
        } ?: messageHandler
            .createMessage(guildId, channelId)
            .withTitle("Aktuell befindet sich kein Track in der Wiedergabe")
            .withWarningColor()
            .build()
    }
}