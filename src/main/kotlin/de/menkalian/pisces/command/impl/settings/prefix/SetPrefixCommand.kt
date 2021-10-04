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
import de.menkalian.pisces.variables.FlunderKey.Flunder
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.settings.prefix.Set"])
class SetPrefixCommand(override val databaseHandler: IDatabaseHandler, val settingsHelper: SettingsCommandHelper) : CommonCommandBase() {
    override fun initialize() {
        supportedContexts.addAll(ALL_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addStringParameter(description = "Das neue Präfix, das verwendet werden soll")

        super.initialize()
    }

    override val name: String
        get() = "setPrefix"
    override val description: String
        get() = "Setzt das Befehlspräfix für diesen Server."

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        var newPrefix = parameters.getDefaultArg()?.asString()
        if (newPrefix == null || newPrefix.isBlank()) {
            newPrefix = databaseHandler.getSettingsValue(0L, Flunder.Guild.Settings.Prefix)
        }

        settingsHelper.createSettingsAction(
            guildId, channelId, Flunder.Guild.Settings.Prefix,
            "Befehlspräfix"
        ).set(newPrefix)
        commandHandler.fireGuildPrefixChanged(guildId, newPrefix)
    }
}