package de.menkalian.pisces.command.impl.base

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.audio.IAudioHandler
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.discord.IDiscordHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.FixedVariables
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.base.Info"])
class InfoCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val audioHandler: IAudioHandler
) : CommonCommandBase() {
    private lateinit var discordHandler: IDiscordHandler

    override fun initialize() {
        aliases.add("status")

        supportedContexts.addAll(ALL_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "info"
    override val description: String
        get() = "Informationen zum aktuellen Status des Bots (auf dem Server)."

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
        messageHandler
            .createMessage(guildId, channelId)
            .withTitle("Statusinformationen")
            .withText(
                """
                    Wiederholung %saktiviert
                    Permanenter Zufallsmix %saktiviert
                    Wiedergabe %spausiert
                """.trimIndent()
                    .format(
                        if (controller.toggleRepeat(false)) "" else "de",
                        if (controller.togglePermanentShuffle(false)) "" else "de",
                        if (controller.togglePause(false)) "" else "nicht ",
                    )
            )
            .build()
    }
}