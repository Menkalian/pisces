package de.menkalian.pisces.discord

import de.menkalian.pisces.BuildConfig
import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.config.IConfig
import de.menkalian.pisces.util.CommonHandlerImpl
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.hooks.EventListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service
import java.util.concurrent.Executors

@Service
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.discord.Handler.JdaDiscordHandler"])
class JdaDiscordHandler(
    @Value("\${pisces.bot.token}") val token: String,
    val config: IConfig,
    val listeners: List<EventListener>
) : IDiscordHandler, CommonHandlerImpl() {

    private lateinit var innerJda: JDA

    override val selfUser: SelfUser
        get() = innerJda.selfUser.let {
            SelfUser(it.idLong, it.name, it.avatarUrl ?: it.effectiveAvatarUrl)
        }

    override val gatewayPing: Long
        get() = innerJda.gatewayPing

    override val restPing: Long
        get() = innerJda.restPing.complete()

    override fun getJdaGuild(id: Long): Guild? {
        return innerJda.getGuildById(id)
    }

    override fun getJdaUser(id: Long): User? {
        return innerJda.getUserById(id)
            ?: innerJda.retrieveUserById(id).complete() ?: null
    }

    override fun installAux() {
        innerJda.installAuxiliaryPort().complete()
    }

    override fun initialize() {
        innerJda = JDABuilder
            .createDefault(token)
            .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "DJ Flunder V${BuildConfig.version}"))
            .setAudioPool(Executors.newScheduledThreadPool(4))
            .setAutoReconnect(true)
            .setEnableShutdownHook(true)
            .setStatus(OnlineStatus.ONLINE)
            .addEventListeners(*listeners.toTypedArray())
            .build()

        innerJda.awaitReady()
        finishInitialization()
    }

    override fun deinitialize() {
        startDeinitialization()
        innerJda.shutdown()
        // Waiting for the shutdown to finish
        Thread.sleep(2000)
    }
}