package de.menkalian.pisces.command.impl.audio.songcontrol

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
 * Implementierung eines Befehls, der eine beliebige Anzahl von Tracks überspringt und diese anschließend erneut queued
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.Reskip"])
class ReskipCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        innerCategory = "Steuerung"

        aliases.add("skiprepeat")
        aliases.add("_r")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addIntParameter(description = "Anzahl der Lieder, die übersprungen werden sollen.", defaultValue = 1)

        super.initialize()
    }

    override val name: String
        get() = "reskip"
    override val description: String
        get() = "Überspringt den aktuellen Song und (falls gewünscht) noch weitere von der Wiedergabeliste und queued diese erneut."

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

        val result = controller.skipTracks(
            skipAmount = parameters.getAmount(),
            requeue = true
        )

        val msg = messageHandler
            .createMessage(guildId, channelId)

        when {
            result.isEmpty() -> {
                msg
                    .withTitle("Es wurden keine Tracks übersprungen")
                    .withWarningColor()
                    .withText("Möglicherweise ist die Queue aktuell leer oder der Aufruf des Befehls war fehlerhaft.")
            }

            result.size == 1 -> {
                val track = result.first()
                msg
                    .withTitle("Ein Track wurde erfolgreich übersprungen und wieder gequeued")
                    .addTrackInfoField(track)
                if (track.sourcetype == AudioSourceType.YOUTUBE)
                    msg.withThumbnail("https://img.youtube.com/vi/${track.sourceIdentifier}/default.jpg")

            }

            else             -> {
                msg.withTitle("Mehrere Tracks wurden erfolgreich übersprungen und erneut gequeued.")
                result.forEachIndexed { index, trackInfo ->
                    msg.addField("${index + 1}. ${trackInfo.title}")
                }
            }
        }

        msg.build()
    }

    private fun List<CommandParameter>.getAmount(): Int {
        return getDefaultArg()
            ?.asInt() ?: 1
    }
}