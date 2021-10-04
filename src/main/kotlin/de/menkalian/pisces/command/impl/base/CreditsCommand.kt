package de.menkalian.pisces.command.impl.base

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.command.CommonCommandBase
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.CommandParameter
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.FixedVariables
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.impl.base.Credits"])
class CreditsCommand(
    override val databaseHandler: IDatabaseHandler,
    val messageHandler: IMessageHandler
) : CommonCommandBase() {

    override fun initialize() {
        supportedContexts.addAll(ALL_CONTEXTS)
        supportedSources.addAll(ALL_SOURCES)

        super.initialize()
    }

    override val name: String
        get() = "credits"
    override val description: String
        get() = "Informationen zum Entwicklerteam"

    override fun execute(
        commandHandler: ICommandHandler,
        source: ECommandSource,
        parameters: List<CommandParameter>,
        guildId: Long,
        channelId: Long,
        authorId: Long,
        sourceInformation: FixedVariables
    ) {
        messageHandler
            .createMessage(guildId, channelId)
            .withTitle("Credits")
            .withAuthor(url = "https://gitlab.com/kiliankra/pisces/-/project_members")
            .withText(
                """
                    Eine Liste der Developer ist hier zu finden: https://gitlab.com/kiliankra/pisces/-/project_members
                    Pisces nutzt JDA als Verbindungswrapper zu Discord und Lavaplayer für die Audioverwaltung.
                    Profilbild erstellt von @Schokito#5358
                """.trimIndent()
            )
            .build()
    }
}