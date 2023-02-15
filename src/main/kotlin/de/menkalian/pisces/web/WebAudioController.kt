package de.menkalian.pisces.web

import de.menkalian.pisces.audio.IAudioHandler
import de.menkalian.pisces.audio.data.AudioSourceType
import de.menkalian.pisces.audio.data.AudioTrackState
import de.menkalian.pisces.audio.data.TrackInfo
import de.menkalian.pisces.util.logger
import de.menkalian.pisces.web.data.AudioPlayerStateData
import de.menkalian.pisces.web.data.NoGuildConnectedException
import de.menkalian.pisces.web.data.NoTrackFoundException
import de.menkalian.pisces.web.data.NoTrackPlayingException
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


@Controller
@RestController
class WebAudioController(
    val messagingTemplate: SimpMessagingTemplate,
    val audioHandler: IAudioHandler
) {
    private val executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())
    private val activeGuilds: MutableSet<Long> = mutableSetOf()
    private val pollerMap: MutableMap<Long, ScheduledFuture<*>> = mutableMapOf()

    @SubscribeMapping("/audio/{id}")
    fun onSubscribe(
        @DestinationVariable("id") id: String
    ) {
        synchronized(executorService) {
            val idLong = id.toLong()
            if (activeGuilds.add(idLong)) {
                logger().info("Starting to send data for guild $id")
                pollerMap[idLong] = executorService.scheduleAtFixedRate(
                    {
                        try {
                            val audioController = audioHandler.getGuildAudioController(id.toLong())
                            val queueInfo = audioController.getQueueInfo()
                            val trackInfo = audioController.getCurrentTrackInfo() ?: EMPTY_TRACK_INFO
                            val playerInfo = AudioPlayerStateData(
                                audioController.togglePause(false),
                                audioController.toggleRepeat(false),
                                audioController.togglePermanentShuffle(false),
                                queueInfo.size,
                                queueInfo.take(5)
                            )
                            messagingTemplate.convertAndSend("/audio/$idLong", trackInfo)
                            messagingTemplate.convertAndSend("/player/$idLong", playerInfo)
                        } catch (ex: Exception) {
                            logger().error("Error", ex)
                        }
                    },
                    0, 500, TimeUnit.MILLISECONDS
                )
            }
        }
    }

    @GetMapping("/audio/guild")
    fun getConnectedGuild(authenticationToken: OAuth2AuthenticationToken): String {
        val audioController = audioHandler.getUserMatchingAudioController(authenticationToken.name.toLong())
        return audioController?.guildId?.let { "\"$it\"" } ?: throw NoGuildConnectedException()
    }

    @PostMapping("/audio/control/play")
    fun playTrack(authenticationToken: OAuth2AuthenticationToken, @RequestBody searchterm: String): TrackInfo {
        val audioController = audioHandler.getUserMatchingAudioController(authenticationToken.name.toLong())
        val result = audioController?.playTrack(searchterm)
        return result?.second?.firstOrNull() ?: throw NoTrackFoundException()
    }

    @GetMapping("/audio/control/skip")
    fun skipTrack(authenticationToken: OAuth2AuthenticationToken): Boolean {
        val audioController = audioHandler.getUserMatchingAudioController(authenticationToken.name.toLong())
        return audioController?.skipTracks()?.isNotEmpty() ?: false
    }

    @PostMapping("/audio/control/skipby")
    fun skipBy(authenticationToken: OAuth2AuthenticationToken, @RequestBody amountSeconds: Long): TrackInfo {
        val audioController = audioHandler.getUserMatchingAudioController(authenticationToken.name.toLong())
        return audioController?.windCurrentTrack(amountSeconds * 1000L) ?: throw NoTrackPlayingException()
    }

    @GetMapping("/audio/control/toggle/loop")
    fun toggleLoop(authenticationToken: OAuth2AuthenticationToken): Boolean {
        val audioController = audioHandler.getUserMatchingAudioController(authenticationToken.name.toLong())
        return audioController?.toggleRepeat(true) ?: false
    }

    @GetMapping("/audio/control/toggle/shuffle")
    fun toggleShuffle(authenticationToken: OAuth2AuthenticationToken): Boolean {
        val audioController = audioHandler.getUserMatchingAudioController(authenticationToken.name.toLong())
        return audioController?.togglePermanentShuffle(true) ?: false
    }

    @GetMapping("/audio/control/toggle/pause")
    fun togglePause(authenticationToken: OAuth2AuthenticationToken): Boolean {
        val audioController = audioHandler.getUserMatchingAudioController(authenticationToken.name.toLong())
        return audioController?.togglePause(true) ?: false
    }

    companion object {
        val EMPTY_TRACK_INFO = TrackInfo(
            "",
            "",
            AudioTrackState.INACTIVE,
            Long.MIN_VALUE,
            Long.MIN_VALUE,
            false,
            AudioSourceType.UNKNOWN,
            "",
            ""
        )
    }
}