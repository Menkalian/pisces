package de.menkalian.pisces.command.impl.settings.shuffle

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
@RequiresKey(["pisces.command.impl.settings.shuffle.Set"])
class SetShuffleCommand(override val databaseHandler: IDatabaseHandler, val settingsHelper: SettingsCommandHelper) : CommonCommandBase() {
    override fun initialize() {
        aliases.add("setDefPermanentShuffle")
        aliases.add("setDefPShuffle")
        aliases.add("setDPS")

        supportedContexts.addAll(ALL_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        addStringParameter(description = "Ob der permanente Shuffle standardmäßig aktiviert sein soll")

        super.initialize()
    }

    override val name: String
        get() = "setDefaultPermanentShuffle"
    override val description: String
        get() = "Setzt das Standardverhalten für den permanenten Zufallsmix nach einem Reset des Audiosystems für diesen Server."

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        val enable = parameters.getDefaultArg()?.asBoolean() ?: false
        settingsHelper.createSettingsAction(
            guildId, channelId, Flunder.Guild.Settings.Shuffle,
            "Standardeinstellung permanenter Zufallsmix"
        ).set(enable.toString())
    }
}