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

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.playlist.AddToPlaylist"])
class AddToPlaylistCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("atp")
        aliases.add("addtopl")
        aliases.add("addpl")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addStringParameter("name", 'n', "Name der Playlist, die verändert werden soll.")
        addStringParameter(description = "Suchbegriff/URL, der/die zu der Playlist hinzugefügt werden soll.")

        super.initialize()
    }

    override val name: String
        get() = "addToPlaylist"
    override val description: String
        get() = "Fügt einen Song/Suchbegriff zu einer Playlist hinzu."

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
        val playlistName = parameters.getName()

        if (!PlaylistHelper.ensurePlaylistValid(playlistName, guildId, channelId, messageHandler)) {
            return
        }

        val searchResult = controller.lookupTracks(parameters.getTextArg())
        val playlist = databaseHandler.getOrCreatePlaylist(guildId, playlistName)
        databaseHandler.addToPlaylist(playlist, searchResult.second.first())

        messageHandler
            .createMessage(guildId, channelId)
            .applyQueueResult(searchResult.copy(second = searchResult.second.subList(0, 1)))
            .withTitle("Ein Track wurde zur Playlist \"$playlistName\" hinzugefügt.")
            .build()
    }

    private fun List<CommandParameter>.getTextArg(): String {
        return getDefaultArg()
            ?.asString() ?: ""
    }

    private fun List<CommandParameter>.getName(): String {
        return filter { it.name == "name" }
            .firstOrNull()
            ?.asString() ?: ""
    }
}