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
@RequiresKey(["pisces.command.impl.audio.playlist.QueueToPlaylist"])
class QueueToPlaylistCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("qtpl")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addStringParameter(description = "Name der Playlist, zu der die aktuelle Wiedergabeliste hinzugefügt werden soll.")

        super.initialize()
    }

    override val name: String
        get() = "queueToPlaylist"
    override val description: String
        get() = "Erstellt eine neue Playlist aus der aktuellen Queue."

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
        val name: String = parameters.getTextArg()
        val playlistHandle = databaseHandler.getOrCreatePlaylist(guildId, name)

        controller
            .getQueueInfo()
            .forEach {
                databaseHandler.addToPlaylist(playlistHandle, it)
            }

        messageHandler
            .createMessage(guildId, channelId)
            .withTitle("Die aktuelle Queue wurde zur Playlist \"$name\" hinzugefügt")
            .build()
    }

    private fun List<CommandParameter>.getTextArg(): String {
        return getDefaultArg()
            ?.asString() ?: ""
    }
}