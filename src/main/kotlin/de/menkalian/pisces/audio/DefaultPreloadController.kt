package de.menkalian.pisces.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist
import de.menkalian.pisces.audio.data.EPlayTrackResult
import de.menkalian.pisces.audio.data.TrackInfo
import de.menkalian.pisces.util.QueueResult
import de.menkalian.pisces.util.SpotifyHelper
import de.menkalian.pisces.util.logger
import java.util.UUID
import java.util.concurrent.CompletableFuture

class DefaultPreloadController(
    private val playerManager: AudioPlayerManager,
    private val spotifyHelper: SpotifyHelper
) : IPreloadController {
    private val preloaded = mutableMapOf<String, AudioTrack>()

    override fun preload(searchterm: String, uuid: String): QueueResult {
        if (preloaded.containsKey(uuid)) {
            logger().debug("Track \"$uuid\" already preloaded. Ignoring redundant preload request.")
            return QueueResult(EPlayTrackResult.TRACK_URL, listOf(preloaded[uuid]!!.makeInfo()))
        }

        logger().info("Searching for \"$searchterm\" and saving it as preloaded")
        val completable = CompletableFuture<QueueResult>()

        val actualSearchterm: String
        val spotifyTrackSearchTerm = spotifyHelper.retrieveFromTrackUrl(searchterm)
        if (spotifyTrackSearchTerm != null) {
            logger().debug("Detected spotify track. Using \"$spotifyTrackSearchTerm\" to find the track on youtube.")
            actualSearchterm = spotifyTrackSearchTerm
        } else {
            // No special case
            actualSearchterm = searchterm
        }

        playerManager.loadItemOrdered(this, actualSearchterm, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack?) {
                logger().debug("Found track for \"$actualSearchterm\": ${track?.makeInfo()}")
                if (track != null) {
                    preloaded[uuid] = track
                    completable.complete(QueueResult(EPlayTrackResult.TRACK_URL, listOf(track.makeInfo())))
                } else {
                    // Try as fallback if invalid data is returned
                    noMatches()
                }
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {
                if (playlist == null || playlist.tracks.isEmpty() || playlist.selectedTrack == null) {
                    // Try as fallback if invalid data is returned
                    noMatches()
                    return
                }

                val selectedTrack = playlist.selectedTrack
                logger().debug("$this found track in playlist ${playlist.name}: ${selectedTrack?.makeInfo()}")
                preloaded[uuid] = selectedTrack
                completable.complete(QueueResult(EPlayTrackResult.TRACK_FROM_PLAYLIST, listOf(selectedTrack.makeInfo())))
            }

            override fun noMatches() {
                logger().debug("$this found no matches for \"$actualSearchterm\". Searching on Youtube...")
                val searchResult = searchYoutube(searchterm)
                if (searchResult.isNotEmpty()) {
                    val trackToAdd = searchResult.first()
                    logger().debug("Using ${trackToAdd.makeInfo()} and saving it as preloaded.")
                    preloaded[uuid] = trackToAdd
                    completable.complete(QueueResult(EPlayTrackResult.TRACK_SEARCH, listOf(trackToAdd.makeInfo())))
                } else {
                    completable.complete(QueueResult(EPlayTrackResult.NOT_FOUND, listOf()))
                }
            }

            override fun loadFailed(exception: FriendlyException?) {
                logger().error("$this failed to load \"$actualSearchterm\".", exception)
                completable.complete(QueueResult(EPlayTrackResult.ERROR, listOf()))
            }
        })

        return completable.get()
    }

    fun getPreloadedTrack(uuid: String) : AudioTrack? {
        return preloaded[uuid]?.makeClone()
    }

    override fun getUniqueUuid(): String {
        var uuid = ""
        while (preloaded.containsKey(uuid) || uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString()
        }
        return uuid
    }


    /**
     * Durchsucht Youtube und gibt die Suchergebnisse der ersten Seite zurück (ca. 25, die genaue Anzahl kann variieren).
     */
    private fun searchYoutube(query: String): List<AudioTrack> {
        val ytSourceManager = playerManager.source(YoutubeAudioSourceManager::class.java) ?: null
        return if (ytSourceManager == null) {
            logger().warn("Kein YoutubeAudioSourceManager registriert.")
            emptyList()
        } else {
            val searchProvider = YoutubeSearchProvider()
            val toReturn: List<AudioTrack> = (searchProvider
                .loadSearchResult(query) { trackInfo ->
                    YoutubeAudioTrack(trackInfo, ytSourceManager)
                } as BasicAudioPlaylist)
                .tracks

            toReturn
        }
    }

    /**
     * Erstellt ein [TrackInfo]-Objekt für den [AudioTrack].
     * Durch diese Methode ist eine bessere Nutzung des Null-Check, bzw. Elvis-Operators möglich, als es mit einem Konstruktor der Fall wäre.
     */
    private fun AudioTrack.makeInfo(): TrackInfo = TrackInfo(this)
}