package de.menkalian.pisces.command.impl.audio.queue

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

/**
 * Implementierung eines Befehls zum Durchmischen der Wiedergabeliste
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.Shuffle"])
class ShuffleCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        innerCategory = "Wiedergabeliste"

        aliases.add("shuf")
        aliases.add("mix")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "shuffle"
    override val description: String
        get() = "Mixt die aktuelle Wiedergabeliste durch"

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
        controller.shuffleQueue()
        messageHandler
            .createMessage(guildId, channelId)
            .withTitle("Die Wiedergabeliste wurde erfolgreich durchgemixt")
            .build()
    }
}