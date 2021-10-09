package de.menkalian.pisces.command.impl.settings

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service

/**
 * Hilfsservice für die Befehle, die Einstellungswerte bearbeiten.
 */
@Service
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.settings"])
class SettingsCommandHelper(val databaseHandler: IDatabaseHandler, val messageHandler: IMessageHandler) {
    /**
     * Erstellt ein [ISettingsAction]-Objekt, das genutzt werden kann um die gewünschte Einstellung zu ändern
     */
    fun createSettingsAction(guildId: Long, channelId: Long, settingsKey: String, settingsReadableName: String = settingsKey): ISettingsAction =
        SettingsAction(databaseHandler, messageHandler, guildId, channelId, settingsKey, settingsReadableName)
}