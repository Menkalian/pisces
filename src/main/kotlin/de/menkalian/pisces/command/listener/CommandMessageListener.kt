package de.menkalian.pisces.command.listener

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.util.Variables
import de.menkalian.pisces.variables.FlunderKey.Flunder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service


@Suppress("RedundantModalityModifier")
@Service
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.CommandMessageListener"])
class CommandMessageListener(final val commandHandler: ICommandHandler, final val databaseHandler: IDatabaseHandler) : ListenerAdapter() {
    private val guildPrefixMap: MutableMap<Long, String> = hashMapOf()
    private val anyMutex = Any()

    init {
        commandHandler.addGuildPrefixChangedListener { id, prefix ->
            guildPrefixMap[id] = prefix
        }
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (!guildPrefixMap.containsKey(event.guild.idLong)) {
            loadGuild(event.guild.idLong)
        }

        val prefix = guildPrefixMap[event.guild.idLong] ?: DEFAULT_PREFIX
        val msg = event.message.contentRaw
        if (msg.startsWith(prefix)) {
            val command = msg.substring(prefix.length)
            executeCommand(command, event)
        }
    }

    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        if (!guildPrefixMap.containsKey(0L)) {
            loadGuild(0L)
        }
        val prefix = guildPrefixMap[0L] ?: DEFAULT_PREFIX
        val msg = event.message.contentRaw
        if (msg.startsWith(prefix)) {
            val command = msg.substring(prefix.length)
            executeCommand(command, event)
        }
    }

    private fun executeCommand(command: String, event: PrivateMessageReceivedEvent) {
        val additionalVars: Variables = hashMapOf()
        additionalVars[Flunder.Command.User.Name] = event.author.name

        commandHandler.executePrivateCommand(command, event.author.idLong, additionalVars)
    }

    private fun executeCommand(command: String, event: GuildMessageReceivedEvent) {
        val additionalVars: Variables = hashMapOf()
        additionalVars[Flunder.Command.User.Name] = event.author.name

        commandHandler.executeGuildCommand(
            command,
            event.guild.idLong,
            ECommandSource.TEXT,
            event.channel.idLong,
            event.channel.type,
            event.author.idLong,
            additionalVars
        )
    }

    private fun loadGuild(id: Long) {
        guildPrefixMap[id] = databaseHandler.getSettingsValue(id, Flunder.Guild.Settings.Prefix.toString(), DEFAULT_PREFIX)
    }

    companion object {
        private const val DEFAULT_PREFIX = "_"
    }
}