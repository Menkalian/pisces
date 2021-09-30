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
import de.menkalian.pisces.util.applyQueueResult
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.Play"])
class PlayCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler,
    val joinCommand: JoinCommand
) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("p")

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

        addBooleanParameter("list", 'l', "Falls diese Option angegeben ist, wird die vollständige Playlist abgespielt, falls eine Playlist gefunden wird.")
        addStringParameter(description = "Suchbegriff oder URL zum abspielen.")

        super.initialize()
    }

    override val name: String
        get() = "play"
    override val description: String
        get() = "Fügt den angegebenen Song zur Queue hinzu. Falls der Bot in keinem Voice-Channel ist, tritt dieser automatisch deinem aktuellen Voice-Channel bei."

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

        val result = controller.playTrack(
            parameters.getTextArg(),
            parameters.isSkipQueue(),
            false,
            parameters.isPlayList()
        )
        messageHandler
            .createMessage(guildId, channelId)
            .applyQueueResult(result)
            .build()
    }

    private fun List<CommandParameter>.getTextArg(): String {
        return this
            .firstOrNull { it.name.isBlank() }
            ?.asString() ?: ""
    }

    private fun List<CommandParameter>.isSkipQueue(): Boolean {
        return this
            .filter { listOf("instant", "now").contains(it.name) }
            .any { it.asBoolean() }
    }

    private fun List<CommandParameter>.isPlayList(): Boolean {
        return this
            .filter { listOf("list").contains(it.name) }
            .any { it.asBoolean() }
    }
}