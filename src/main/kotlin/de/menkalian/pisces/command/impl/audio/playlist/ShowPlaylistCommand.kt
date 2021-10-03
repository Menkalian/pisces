package de.menkalian.pisces.command.impl.audio.playlist

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.FixedVariables
import de.menkalian.pisces.util.toDurationString
import de.menkalian.pisces.variables.FlunderKey.Flunder
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.playlist.Show"])
class ShowPlaylistCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler
) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("showpl")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addStringParameter(description = "Name der Playlist, deren Lieder angezeigt werden sollen.")

        super.initialize()
    }

    override val name: String
        get() = "showplaylist"
    override val description: String
        get() = "Zeigt alle Playlists des Servers oder die Lieder einer einzelnen Playlist."

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
        val msg = messageHandler
            .createMessage(guildId, channelId)
        if (name.isBlank()) {
            msg.withTitle(
                "Playlisten für %s"
                    .format(sourceInformation[Flunder.Command.Guild.Name])
            )

            databaseHandler
                .getPlaylists(guildId)
                .forEach {
                    msg.appendText("- $it\n")
                }
        } else {
            val playlistHandle = databaseHandler.getPlaylistIfExists(guildId, name)
            if (playlistHandle != null) {
                val songs = databaseHandler.getPlaylistSongs(playlistHandle)
                msg.withTitle("Inhalt der Playlist \"$name\"")
                songs.forEachIndexed { nr, entry ->
                    msg.addField(
                        "%d. %s".format(nr + 1, entry.title),
                        """
                            Url: ${entry.url}
                            Duration: ${entry.duration.toDurationString()}
                        """.trimIndent()
                    )
                }
            } else {
                msg
                    .withColor(red = 255.toByte())
                    .withTitle("Die angegebene Playlist \"$name\" existiert nicht.")
            }
        }

        msg.build()
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