package de.menkalian.pisces.command.impl.audio.songcontrol

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
import de.menkalian.pisces.util.addTrackInfoField
import de.menkalian.pisces.util.withWarningColor
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.Wind"])
class WindCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("skipby")
        aliases.add("rewind")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addIntParameter(
            description = "Zeit (in Sekunden) um die die aktuelle Wiedergabezeit verschoben werden soll (negativ zum Zurückspulen)",
            defaultValue = 0
        )

        super.initialize()
    }

    override val name: String
        get() = "wind"
    override val description: String
        get() = "Verschiebt die aktuelle Wiedergabeposition im aktuellen Track."

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
        controller.windCurrentTrack((parameters.getDefaultArg()?.asInt() ?: 0) * 1000L)

        controller.getCurrentTrackInfo()?.let {
            messageHandler
                .createMessage(guildId, channelId)
                .withTitle("Die Position des Tracks wurde angepasst.")
                .addTrackInfoField(it, true)
                .build()
        } ?: messageHandler
            .createMessage(guildId, channelId)
            .withTitle("Aktuell befindet sich kein Track in der Wiedergabe")
            .withWarningColor()
            .build()
    }
}