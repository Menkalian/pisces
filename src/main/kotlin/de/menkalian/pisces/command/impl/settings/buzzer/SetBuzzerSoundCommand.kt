package de.menkalian.pisces.command.impl.settings.joinsound

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.audio.IAudioHandler
import de.menkalian.pisces.audio.data.TrackInfo
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.FixedVariables
import de.menkalian.pisces.util.addTrackInfoField
import de.menkalian.pisces.util.asInlineCode
import de.menkalian.pisces.util.withErrorColor
import de.menkalian.pisces.util.withSuccessColor
import de.menkalian.pisces.util.withWarningColor
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

/**
 * Implementierung eines Befehls zum Setzen der aktuellen Joinsound-Einstellungen
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.settings.buzzer.Set"])
class SetBuzzerSoundCommand(override val databaseHandler: IDatabaseHandler, val audioHandler: IAudioHandler, val messageHandler: IMessageHandler) :
    CommonCommandBase() {
    override fun initialize() {
        innerCategory = "Tool"

        aliases.add("setbuz")

        supportedContexts.addAll(ALL_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addStringParameter(description = "Suchbegriff oder (bevorzugt) direkter Link zum Joinsound (max. 5 Sekunden Dauer)")
        super.initialize()
    }

    override val name: String
        get() = "setBuzzer"
    override val description: String
        get() = "Setzt Server Buzzersound"

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        if (guildId == 0L) {
            messageHandler
                .createMessage(guildId, channelId)
                .withTitle("Die Einstellung muss auf einem Server vorgenommen werden.")
                .withErrorColor()
                .build()
            return
        }

        val controller = audioHandler.getGuildAudioController(guildId)

        val foundAudio = controller.lookupTracks(parameters.getDefaultArg()?.asString() ?: "")
        val foundTrack = foundAudio.second.firstOrNull()

        val validityCheckResult = checkTrackValidity(foundTrack)
        if (foundTrack != null && validityCheckResult.isBlank()) {
            // valid
            databaseHandler.setGuildBuzzersound(guildId, foundTrack)
            messageHandler
                .createMessage(guildId, channelId)
                .withTitle("Buzzersound wurde aktualisiert")
                .addTrackInfoField(foundTrack)
                .withSuccessColor()
                .build()
        } else {
            // not valid
            messageHandler
                .createMessage(guildId, channelId)
                .withTitle("Buzzersound konnte nicht gesetzt werden")
                .withText("Fehlermeldung: ${validityCheckResult.asInlineCode()}")
                .withWarningColor()
                .build()
        }
    }

    private fun checkTrackValidity(trackInfo: TrackInfo?): String {
        return when {
            trackInfo == null       -> "No Track found"
            trackInfo.length > 6000 -> "Track too long" // Officially only up to 5s are allowed
            else                    -> ""
        }
    }
}