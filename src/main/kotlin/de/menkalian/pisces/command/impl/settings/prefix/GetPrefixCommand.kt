package de.menkalian.pisces.command.impl.settings.prefix

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.command.impl.settings.SettingsCommandHelper
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.util.FixedVariables
import de.menkalian.pisces.variables.FlunderKey
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.settings.prefix.Get"])
class GetPrefixCommand(
    override val databaseHandler: IDatabaseHandler,
    val settingsHelper: SettingsCommandHelper
) : CommonCommandBase() {
    override fun initialize() {
        supportedContexts.addAll(ALL_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "getPrefix"
    override val description: String
        get() = "Zeigt das aktuelle Präfix für diesen Server an."

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
            guildId, channelId, FlunderKey.Flunder.Guild.Settings.Prefix,
            "Befehlspräfix"
        ).notifyCurrent()
    }
}