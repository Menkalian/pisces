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
import de.menkalian.pisces.util.SpotifyHelper
import de.menkalian.pisces.util.applyQueueResult
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
    val spotifyHelper: SpotifyHelper?,
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

        val name: String = parameters.getTextArg()

        messageHandler
            .createMessage(guildId, channelId)
            .withTitle("Suche Playlist und lade Songs... Je nach Art der Playlist kann dies einige Zeit dauern.")
            .withSuccessColor()
            .build()
        val result = controller.playList(name, shuffled = true)
        messageHandler
            .createMessage(guildId, channelId)
            .applyQueueResult(result)
            .build()
    }

    private fun List<CommandParameter>.getTextArg(): String {
        return getDefaultArg()
            ?.asString() ?: ""
    }

}