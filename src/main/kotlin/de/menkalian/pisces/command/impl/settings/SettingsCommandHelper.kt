package de.menkalian.pisces.command.impl.settings

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service

@Service
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.settings"])
class SettingsCommandHelper(val databaseHandler: IDatabaseHandler, val messageHandler: IMessageHandler) {
    fun createSettingsAction(guildId: Long, channelId: Long, settingsKey: String, settingsReadableName: String = settingsKey): ISettingsAction =
        SettingsAction(databaseHandler, messageHandler, guildId, channelId, settingsKey, settingsReadableName)
}