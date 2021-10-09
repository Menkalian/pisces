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
import de.menkalian.pisces.util.withErrorColor
import de.menkalian.pisces.util.withSuccessColor
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

/**
 * Implementierung eines Befehls zur zufälligen Wiedergabe einer Playlist
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.playlist.PlayListShuffled"])
class PlayListShuffledCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler,
    val joinCommand: JoinCommand
) : CommonCommandBase() {
    override fun initialize() {
        innerCategory = "Wiedergabe"

        aliases.add("spl")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addStringParameter(description = "Name der Playlist oder URL der externen Playlist, die abgespielt werden soll.")

        super.initialize()
    }

    override val name: String
        get() = "shuffledplaylist"
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

        val playlistName: String = parameters.getTextArg()
        if (!PlaylistHelper.ensurePlaylistValid(playlistName, guildId, channelId, messageHandler)) {
            return
        }

        val playlistHandle = databaseHandler.getPlaylistIfExists(guildId, playlistName)
        if (playlistHandle != null) {
            val songs = databaseHandler.getPlaylistSongs(playlistHandle)
            val msg = messageHandler
                .createMessage(guildId, channelId)

            songs
                .shuffled()
                .forEachIndexed { index, track ->
                    val result = controller
                        .playTrack(
                            track.url,
                            playInstant = index == 0
                        )
                    msg.applyQueueResult(result)
                }

            msg
                .withThumbnail("")
                .withSuccessColor()
                .withTitle("Die Playlist $playlistName wurde geladen und in zufälliger Reihenfolge zur Queue hinzugefügt.")
                .build()
        } else {
            messageHandler
                .createMessage(guildId, channelId)
                .withErrorColor()
                .withTitle("Eine Playlist namens \"$playlistName\" existiert nicht auf dem Server")
                .build()
        }
    }

    private fun List<CommandParameter>.getTextArg(): String {
        return getDefaultArg()
            ?.asString() ?: ""
    }

}