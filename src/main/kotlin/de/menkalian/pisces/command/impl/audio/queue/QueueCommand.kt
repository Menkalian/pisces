package de.menkalian.pisces.command.impl.audio.queue

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.audio.IAudioHandler
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.Emoji
import de.menkalian.pisces.util.FixedVariables
import de.menkalian.pisces.util.addTrackInfoField
import de.menkalian.pisces.util.toDurationString
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.Queue"])
class QueueCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("q")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "queue"
    override val description: String
        get() = "Zeigt die aktuelle Wiedergabeliste an."

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
        val result = controller.getQueueInfo()

        val msg = messageHandler
            .createMessage(guildId, channelId)

        if (result.isEmpty()) {
            msg.withTitle("Aktuell ist die Wiedergabeliste leer. Füg doch etwas hinzu ${Emoji.SLIGHT_SMILE}.")
        } else {
            msg.withTitle("Aktuelle Wiedergabeliste")
                .withText(
                    "Gesamtdauer: %s".format(
                        result.sumOf { it.length }.toDurationString()
                    )
                )
            result.forEachIndexed { index, trackInfo ->
                msg.addTrackInfoField(trackInfo.copy(title = "${index + 1}: ${trackInfo.title}"))
            }
        }

        msg.build()
    }
}