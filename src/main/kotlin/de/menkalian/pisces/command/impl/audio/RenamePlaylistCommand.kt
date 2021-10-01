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
@RequiresKey(["pisces.command.impl.audio.playlist.RenamePlaylist"])
class RenamePlaylistCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("mvpl")
        aliases.add("rnpl")
        aliases.add("renamepl")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addStringParameter("name", 'n', "Name der Playlist, die verändert werden soll.")
        addStringParameter(description = "Neuer Name der Playlist.")

        super.initialize()
    }

    override val name: String
        get() = "renamePlaylist"
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
        val oldName = parameters.getName()
        val newName = parameters.getTextArg()
        val plExists = databaseHandler.getPlaylistIfExists(guildId, newName) == null
        if (!plExists) {
            databaseHandler.renamePlaylist(
                databaseHandler.getOrCreatePlaylist(guildId, oldName),
                newName
            )
        }

        messageHandler
            .createMessage(guildId, channelId)
            .withTitle(
                if (plExists)
                    "Die Playlist \"$newName\" existiert bereits. Ein Umbenennen ist nicht möglich."
                else
                    "Die Playlist \"$oldName\" wurde zu \"$newName\" umbenannt."
            )
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