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
import de.menkalian.pisces.util.logger
import de.menkalian.pisces.util.withErrorColor
import de.menkalian.pisces.util.withSuccessColor
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

/**
 * Implementierung eines Befehls zum Zurücksetzen des Audioplayers
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.audio.Reset"])
class ResetCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    override fun initialize() {
        innerCategory = "Audio"

        aliases.add("rst")

        supportedContexts.addAll(ALL_GUILD_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "reset"
    override val description: String
        get() = "Setzt den Audio-Controller für den Server zurück"

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        try {
            audioHandler.deleteGuildAudioController(guildId)
            audioHandler.getGuildAudioController(guildId)
            messageHandler
                .createMessage(guildId, channelId)
                .withTitle("AudioController erfolgreich zurückgesetzt")
                .withSuccessColor()
                .build()
        } catch (ex: Exception) {
            logger().error("An exception occured when reseting the GuildAudioController for $guildId")
            messageHandler
                .createMessage(guildId, channelId)
                .withTitle("Ein Fehler ist beim Zurücksetzen aufgetreten")
                .withErrorColor()
                .build()
        }
    }
}