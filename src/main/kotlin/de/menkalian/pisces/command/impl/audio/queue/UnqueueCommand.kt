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

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.Unqueuelast"])
class UnqueueCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("uq")
        aliases.add("rm")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addIntParameter(description = "Position des Songs, der gelöscht werden soll.", defaultValue = 1)

        super.initialize()
    }

    override val name: String
        get() = "unqueue"
    override val description: String
        get() = "Löscht den Song an der angegebenen Position aus der Wiedergabeliste"

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
        val deleteIndex = (parameters.getDefaultArg()?.asInt() ?: 1) - 1
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