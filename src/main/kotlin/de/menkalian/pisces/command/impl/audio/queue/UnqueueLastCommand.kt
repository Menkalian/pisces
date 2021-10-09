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
import de.menkalian.pisces.util.addTrackInfoField
import de.menkalian.pisces.util.withWarningColor
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

/**
 * Implementierung eines Befehls zum Entfernen des zuletzt gequeueten Tracks aus der Wiedergabeliste
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.Unqueuelast"])
class UnqueueLastCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        innerCategory = "Wiedergabeliste"

        aliases.add("uql")
        aliases.add("pop")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "unqueueLast"
    override val description: String
        get() = "Löscht den letzten Song aus der Wiedergabeliste"

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
        val deleteIndex = controller.getQueueInfo().size - 1
        val deleted = controller.deleteFromQueue(deleteIndex)

        val msg = messageHandler
            .createMessage(guildId, channelId)

        if (deleted != null) {
            msg
                .addTrackInfoField(deleted)
                .withTitle("Der Track wurde aus der Queue entfernt")
        } else {
            msg
                .withTitle("Es konnte kein Track aus der Queue gelöscht werden")
                .withWarningColor()
        }

        msg.build()
    }
}