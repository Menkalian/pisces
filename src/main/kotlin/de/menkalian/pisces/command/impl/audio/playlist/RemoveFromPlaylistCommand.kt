package de.menkalian.pisces.command.impl.audio.playlist

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
import de.menkalian.pisces.util.applyQueueResult
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component


/**
 * Implementierung eines Befehls, der einen Song aus einer Playlist entfernt
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.playlist.RemoveFromPlaylist"])
class RemoveFromPlaylistCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        innerCategory = "Playlist"

        aliases.add("rfp")
        aliases.add("rmfrompl")
        aliases.add("rmpl")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addStringParameter("name", 'n', "Name der Playlist, die verändert werden soll.")
        addStringParameter(description = "Suchbegriff/URL, der/die aus der Playlist entfernt werden soll.")

        super.initialize()
    }

    override val name: String
        get() = "removeFromPlaylist"
    override val description: String
        get() = "Entfernt einen Song/Suchbegriff aus einer Playlist."

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        val playlistName = parameters.getName()
        if (!PlaylistHelper.ensurePlaylistValid(playlistName, guildId, channelId, messageHandler)) {
            return
        }

        val controller = audioHandler.getGuildAudioController(guildId)
        val searchResult = controller.lookupTracks(parameters.getTextArg())
        val playlist = databaseHandler.getOrCreatePlaylist(guildId, playlistName)
        databaseHandler.removeFromPlaylist(playlist, searchResult.second.first())

        messageHandler
            .createMessage(guildId, channelId)
            .applyQueueResult(searchResult.copy(second = searchResult.second.subList(0, 1)))
            .withTitle("Ein Track wurde aus der Playlist \"$playlistName\" entfernt.")
            .build()
    }

    private fun List<CommandParameter>.getTextArg(): String {
        return getDefaultArg()
            ?.asString() ?: ""
    }

    private fun List<CommandParameter>.getName(): String {
        return firstOrNull { it.name == "name" }
            ?.asString() ?: ""
    }
}