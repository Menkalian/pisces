package de.menkalian.pisces.command.impl.audio.playlist

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.audio.IAudioHandler
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.command.impl.audio.JoinCommand
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.FixedVariables
import de.menkalian.pisces.util.applyQueueResult
import de.menkalian.pisces.util.withSuccessColor
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

/**
 * Implementierung eines Befehls zur Wiedergabe einer Playlist
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.playlist.PlayList"])
class PlayListCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler,
    val joinCommand: JoinCommand
) : CommonCommandBase() {
    override fun initialize() {
        innerCategory = "Wiedergabe"

        aliases.add("pl")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addBooleanParameter(
            "instant",
            'i',
            "Falls diese Option übergeben wurde, wird die aktuelle Queue übersprungen, die aktuelle Wiedergabe abgebrochen und der Song wird sofort abgespielt."
        )
        addBooleanParameter(
            "now",
            'n',
            "Falls diese Option übergeben wurde, wird die aktuelle Queue übersprungen, die aktuelle Wiedergabe abgebrochen und der Song wird sofort abgespielt."
        )

        addStringParameter(description = "Name der Playlist oder URL der externen Playlist, die abgespielt werden soll.")

        super.initialize()
    }

    override val name: String
        get() = "playlist"
    override val description: String
        get() = "Fügt die angegebene Playlist zur Queue hinzu."

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
        if (controller.getConnectedChannel() == null) {
            joinCommand.execute(commandHandler, source, parameters, guildId, channelId, authorId, sourceInformation)
        }

        val name: String = parameters.getTextArg()
        val playlistHandle = databaseHandler.getPlaylistIfExists(guildId, name)
        if (playlistHandle != null) {
            val songs = databaseHandler.getPlaylistSongs(playlistHandle)
            val msg = messageHandler
                .createMessage(guildId, channelId)

            songs.forEachIndexed { index, track ->
                val result = controller
                    .playTrack(
                        track.url,
                        playInstant = index == 0 && parameters.isSkipQueue()
                    )
                msg.applyQueueResult(result)
            }

            msg
                .withThumbnail("")
                .withSuccessColor()
                .withTitle("Die Playlist $name wurde geladen.")
                .build()
        } else {
            val result = controller.playTrack(
                parameters.getTextArg(),
                parameters.isSkipQueue(),
                interruptCurrent = false,
                playFullPlaylist = true
            )
            messageHandler
                .createMessage(guildId, channelId)
                .applyQueueResult(result)
                .build()
        }
    }

    private fun List<CommandParameter>.getTextArg(): String {
        return getDefaultArg()
            ?.asString() ?: ""
    }

    private fun List<CommandParameter>.isSkipQueue(): Boolean {
        return this
            .filter { listOf("instant", "now").contains(it.name) }
            .any { it.asBoolean() }
    }
}