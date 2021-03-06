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

/**
 * Implementierung eines Befehls zum Suchen von Audiotracks
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.Search"])
class SearchCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        innerCategory = "Audio"

        aliases.add("s")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addIntParameter("count", 'c', "Anzahl der Ergebnisse, die ausgegeben werden sollen", 5)
        addStringParameter(description = "Suchbegriff")

        super.initialize()
    }

    override val name: String
        get() = "search"
    override val description: String
        get() = "Sucht den angegebenen Begriff auf Youtube und gibt die angegebene Zahl von Ergebnissen aus"

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
        val result = controller.lookupTracks(
            parameters.getTextArg(),
            true,
            parameters.getCount()
        )
        messageHandler
            .createMessage(guildId, channelId)
            .applyQueueResult(result)
            .withTitle("Suchergebnisse")
            .build()
    }

    private fun List<CommandParameter>.getTextArg(): String {
        return getDefaultArg()
            ?.asString() ?: ""
    }

    private fun List<CommandParameter>.getCount(): Int {
        return firstOrNull { it.name == "count" }
            ?.asInt() ?: 0
    }
}