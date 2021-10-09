package de.menkalian.pisces.command.impl.settings

import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.logger
import de.menkalian.pisces.util.withErrorColor
import de.menkalian.pisces.util.withSuccessColor

/**
 * Standardimplementierung von [ISettingsAction]
 */
class SettingsAction(
    val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler,
    val guildId: Long,
    val channelId: Long,
    val settingsKey: String,
    val settingsReadableName: String
) : ISettingsAction {

    override fun get(): String {
        val settingsValue = databaseHandler.getSettingsValue(guildId, settingsKey)
        logger().debug("$settingsReadableName ($settingsKey) has \"$settingsValue\" as value")
        return settingsValue
    }

    override fun notifyCurrent() {
        messageHandler
            .createMessage(guildId, channelId)
            .withTitle("Der aktuelle Wert für $settingsReadableName ist ${get()}")
            .build()
    }

    override fun set(value: String) {
        if (guildId == 0L) {
            messageHandler
                .createMessage(guildId, channelId)
                .withTitle("Einstellungen dürfen nur für Server getätigt werden. Globale Einstellungen sind nicht möglich.")
                .withErrorColor()
                .build()
        } else {
            databaseHandler.setSettingsValue(guildId, settingsKey, value)
            messageHandler
                .createMessage(guildId, channelId)
                .withTitle("$settingsReadableName wurde auf den Wert \"$value\" gesetzt.")
                .withSuccessColor()
                .build()
        }
    }
}