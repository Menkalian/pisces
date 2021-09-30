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
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.Skip"])
class SkipCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("next")
        aliases.add("_")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addBooleanParameter(
            "requeue",
            'r',
            "Falls diese Option übergeben wurde, werden die übersprungenen Lieder wieder an die aktuelle Queue angehängt."
        )

        addIntParameter(description = "Anzahl der Lieder, die übersprungen werden sollen.", defaultValue = 1)

        super.initialize()
    }

    override val name: String
        get() = "skip"
    override val description: String
        get() = "Überspringt den aktuellen Song und (falls gewünscht) noch weitere von der Wiedergabeliste."

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
            requeue = parameters.isRequeue()
        )

        val msg = messageHandler
            .createMessage(guildId, channelId)

        when {
            result.isEmpty() -> {
                msg
                    .withTitle("Es wurden keine Tracks übersprungen")
                    .withColor(red = 255.toByte(), green = 136.toByte())
                    .withText("Möglicherweise ist die Queue aktuell leer oder der Aufruf des Befehls war fehlerhaft.")
            }
            result.size == 1 -> {
                val track = result.first()
                msg
                    .withTitle("Ein Track wurde erfolgreich übersprungen")
                    .addTrackInfoField(track)
                if (track.sourcetype == AudioSourceType.YOUTUBE)
                    msg.withThumbnail("https://img.youtube.com/vi/${track.sourceIdentifier}/default.jpg")

            }
            else             -> {
                msg.withTitle("Mehrere Tracks wurden erfolgreich übersprungen.")
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

    private fun List<CommandParameter>.isRequeue(): Boolean {
        return this
            .filter { listOf("requeue").contains(it.name) }
            .any { it.asBoolean() }
    }
}