package de.menkalian.pisces.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener
import de.menkalian.pisces.audio.data.TrackInfo
import de.menkalian.pisces.audio.sending.AudioSendHandlerFactory
import de.menkalian.pisces.discord.IDiscordHandler
import de.menkalian.pisces.util.QueueResult
import de.menkalian.pisces.util.logger
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.managers.AudioManager

class JdaGuildAudioController(
    val discordHandler: IDiscordHandler,
    val guildId: Long,
    private val playerManager: AudioPlayerManager,
    private val sendHandlerFactory: AudioSendHandlerFactory
) : IGuildAudioController,
    AudioEventListener,
    AudioEventAdapter() {
    private val player: AudioPlayer
    private val jdaGuildAudioManager: AudioManager

    init {
        this.logger().debug("Initializing $this")
        this.logger().trace("Initializing AudioPlayer")
        player = playerManager.createPlayer()!!

        this.logger().trace("Configuring AudioPlayer")
        player.addListener(this)

        this.logger().trace("AudioPlayer setup complete.")

        this.logger().debug("Setting up JDA GuildAudioManager")
        jdaGuildAudioManager = discordHandler.jda
            .getGuildById(guildId)
            ?.audioManager ?: throw IllegalArgumentException("Invalid GuildId")
        jdaGuildAudioManager.sendingHandler = sendHandlerFactory.createAudioPlayerSendHandler(player)

        this.logger().info("Initialization of $this complete.")
    }

    override fun connect(channelId: Long): Boolean {
        val channel = jdaGuildAudioManager.guild.getGuildChannelById(channelId)
        return if (channel is VoiceChannel) {
            try {
                jdaGuildAudioManager.openAudioConnection(channel)
                true
            } catch (ex: Exception) {
                logger().error("$this could not open a connection to channel $channelId (${channel.name})", ex)
                TODO("NOTIFY")
                false
            }
        } else {
            TODO("NOTIFY")
            false
        }
    }

    override fun disconnect(): Boolean {
        val currentChannel = jdaGuildAudioManager.connectedChannel
        if (currentChannel == null) {
            TODO("NOTIFY")
        } else {
            TODO("NOTIFY")
        }
        jdaGuildAudioManager.closeAudioConnection()
    }

    override fun reset() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun playTrack(searchterm: String, playInstant: Boolean, interruptCurrent: Boolean, playFullPlaylist: Boolean): QueueResult {
        TODO("Not yet implemented")
    }

    override fun getSearchResult(searchterm: String, enableSearch: Boolean, results: Int): QueueResult {
        TODO("Not yet implemented")
    }

    override fun skipTracks(requeue: Boolean, skipAmount: Int): List<TrackInfo> {
        TODO("Not yet implemented")
    }

    override fun windCurrentTrack(deltaMs: Long): TrackInfo {
        TODO("Not yet implemented")
    }

    override fun deleteFromQueue(index: Int): TrackInfo {
        TODO("Not yet implemented")
    }

    override fun clearQueue() {
        TODO("Not yet implemented")
    }

    override fun shuffleQueue() {
        TODO("Not yet implemented")
    }

    override fun getCurrentTrackInfo(): TrackInfo? {
        TODO("Not yet implemented")
    }

    override fun getQueueInfo(): List<TrackInfo> {
        TODO("Not yet implemented")
    }

    override fun togglePause(readOnly: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun toggleRepeat(readOnly: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun togglePermanentShuffle(readOnly: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "JdaGuildAudioController(guildId=$guildId)"
    }

    private fun createSendingHandler(player: AudioPlayer): AudioSendHandler {
        TODO("not yet implemented")
    }
}