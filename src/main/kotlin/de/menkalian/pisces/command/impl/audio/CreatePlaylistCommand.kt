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
@RequiresKey(["pisces.command.impl.audio.playlist.CreatePlaylist"])
class CreatePlaylistCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("createpl")
        aliases.add("mkpl")
        aliases.add("makepl")
        aliases.add("makeplaylist")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addStringParameter(description = "Name der Playlist, zu der die aktuelle Wiedergabeliste hinzugefügt werden soll.")

        super.initialize()
    }

    override val name: String
        get() = "createPlaylist"
    override val description: String
        get() = "Erstellt eine neue Playlist."

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        val name: String = parameters.getTextArg()
        val plExists = databaseHandler.getPlaylistIfExists(guildId, name) == null
        if (!plExists) {
            databaseHandler.getOrCreatePlaylist(guildId, name)
        }

        messageHandler
            .createMessage(guildId, channelId)
            .withTitle(
                if (plExists)
                    "Die Playlist \"$name\" existiert bereits."
                else
                    "Die neue Playlist \"$name\" wurde angelegt."
            )
            .build()
    }

    private fun List<CommandParameter>.getTextArg(): String {
        return getDefaultArg()
            ?.asString() ?: ""
    }
}