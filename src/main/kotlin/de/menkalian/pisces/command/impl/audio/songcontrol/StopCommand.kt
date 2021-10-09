package de.menkalian.pisces.command.impl.audio.songcontrol

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
 * Implementierung eines Befehls zum Stoppen der Wiedergabe (ohne die Wiedergabeliste zu beeinflussen)
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.Stop"])
class StopCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        innerCategory = "Steuerung"

        aliases.add("st")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "stop"
    override val description: String
        get() = "Beendet die aktuelle Wiedergabe, behält aber die Queue bei (das nächste Lied wird allerdings nicht gestartet)."

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
        controller.stop()

        messageHandler
            .createMessage(guildId, channelId)
            .withTitle("Die Wiedergabe wurde angehalten")
            .build()
    }
}