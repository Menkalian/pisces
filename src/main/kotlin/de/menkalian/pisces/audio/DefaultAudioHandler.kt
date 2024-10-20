package de.menkalian.pisces.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.audio.sending.AudioSendHandlerFactory
import de.menkalian.pisces.config.IConfig
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.discord.IDiscordHandler
import de.menkalian.pisces.util.*
import dev.lavalink.youtube.YoutubeAudioSourceManager
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

/**
 * Standardimplementierung der [IAudioHandler]-Schnittstelle.
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.audio.Handler.JdaAudioHandler"])
class DefaultAudioHandler(
    val databaseHandler: IDatabaseHandler,
    val config: IConfig,
    val audioSendHandlerFactory: AudioSendHandlerFactory,
    val spotifyHelper: SpotifyHelper,
    val beanFactory: BeanFactory,
    @Value("\${pisces.youtube.oauth2.token}") val youtubeRefreshToken: String?,
) : IAudioHandler,
    CommonHandlerImpl() {
    val controllerLock = Any()

    val controllers: HashMap<Long, IGuildAudioController> = hashMapOf()
    lateinit var discordHandler: IDiscordHandler
    lateinit var playerManager: AudioPlayerManager
    override lateinit var preloadController: DefaultPreloadController

    override fun getGuildAudioController(guildId: Long): IGuildAudioController {
        val result = synchronized(controllerLock) {
            if (!controllers.containsKey(guildId)) {
                logger().debug("Controller for $guildId does not exist. Creating a new one.")
                controllers[guildId] = createAudioController(guildId)
            }
            controllers[guildId] ?: throw RuntimeException("GuildController could not be created")
        }
        return result
    }

    override fun deleteGuildAudioController(guildId: Long): Boolean {
        synchronized(controllerLock) {
            logger().info("Deleting GuildAudioController for $guildId")
            return controllers.containsKey(guildId) && controllers.remove(guildId) != null
        }
    }

    override fun getUserMatchingAudioController(userId: Long): IGuildAudioController? {
        return synchronized(controllerLock) {
            val userConnectedFilter = controllers.values.filter {
                it.getUserVoiceChannelId(userId) != null
            }

            if (userConnectedFilter.size > 1) {
                userConnectedFilter.filter {
                    it.getConnectedChannel() == it.getUserVoiceChannelId(userId)
                }.firstOrNull() ?: userConnectedFilter.firstOrNull()
            } else {
                userConnectedFilter.firstOrNull()
            }
        }
    }

    private fun createAudioController(id: Long): IGuildAudioController {
        config.featureConfig.pisces.audio.Controller.let {
            if (it.isEnabled.not())
                throw IllegalStateException("AudioController may not be inactive in Config, if AudioHandler is active")

            return when (it.activeImplementation) {
                it.JdaGuildAudioController -> JdaGuildAudioController(
                    id,
                    audioSendHandlerFactory,
                    preloadController,
                    playerManager,
                    discordHandler,
                    databaseHandler,
                    spotifyHelper
                )

                else                       -> throw IllegalStateException("Unknown pisces.audio.Controller implementation ative")
            }
        }
    }

    override fun initialize() {
        discordHandler = beanFactory.getBean(IDiscordHandler::class.java)

        // Initialize the AudioPlayerManager
        playerManager = DefaultAudioPlayerManager()
        AudioSourceManagers.registerLocalSource(playerManager)
        @Suppress("DEPRECATION") // We exclude it for its deprecation.
        AudioSourceManagers.registerRemoteSources(playerManager, com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager::class.java)
        val youtubeSourceManager = YoutubeAudioSourceManager()
        youtubeSourceManager.useOauth2(
            youtubeRefreshToken?.takeIf { it.isNotBlank() },
            youtubeRefreshToken != null && youtubeRefreshToken!!.isNotBlank()
        )
        playerManager.registerSourceManager(youtubeSourceManager)

        preloadController = DefaultPreloadController(playerManager, spotifyHelper)

        finishInitialization()
    }

    override fun deinitialize() {
        startDeinitialization()

        playerManager.shutdown()
    }
}