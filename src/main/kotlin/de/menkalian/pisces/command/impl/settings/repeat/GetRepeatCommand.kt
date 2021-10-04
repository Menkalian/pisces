package de.menkalian.pisces.command.impl.settings.repeat

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.command.impl.settings.SettingsCommandHelper
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.util.FixedVariables
import de.menkalian.pisces.variables.FlunderKey.Flunder
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.settings.repeat.Get"])
class GetRepeatCommand(override val databaseHandler: IDatabaseHandler, val settingsHelper: SettingsCommandHelper) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("getDefRepeat")
        aliases.add("getDR")

        supportedContexts.addAll(ALL_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "getDefaultRepeat"
    override val description: String
        get() = "Zeigt das Standardverhalten für die Wiederholung nach einem Reset des Audiosystems für diesen Server an."

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        settingsHelper.createSettingsAction(
            guildId, channelId, Flunder.Guild.Settings.Repeat,
            "Standardeinstellung Wiederholung"
        ).notifyCurrent()
    }
}