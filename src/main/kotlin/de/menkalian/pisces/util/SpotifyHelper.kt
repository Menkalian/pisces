package de.menkalian.pisces.util

import com.wrapper.spotify.SpotifyApi
import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Helfer für die Kommunikation mit Spotify's API
 */
@Service
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.audio.spotify.SpotifyHelper"])
class SpotifyHelper(
    @Value("\${pisces.spotify.client}") val clientId: String,
    @Value("\${pisces.spotify.secret}") val clientSecret: String
) {
    private val apiMutex = Any()
    final val spotifyApi: SpotifyApi?

    val playlistIdPattern = "https://open.spotify.com/playlist/([a-zA-z0-9]+)\\?.+".toRegex().toPattern()

    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private var renewErrorCount = 0L

    private inner class RefreshTokenRunnable : Runnable {
        override fun run() {
            val nextRun = refreshToken()
            scheduler.schedule(this, nextRun, TimeUnit.SECONDS)
        }
    }

    init {
        var tmpSpotifyApi: SpotifyApi?
        try {
            tmpSpotifyApi = SpotifyApi.builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build()!!
            scheduler.schedule(RefreshTokenRunnable(), 0L, TimeUnit.SECONDS)
        } catch (ex: Exception) {
            tmpSpotifyApi = null
        }
        spotifyApi = tmpSpotifyApi
    }

    /**
     * Prüft ob der angegebene String eine Spotify-URL ist und lädt gegebenenfalls die Lieder davon.
     */
    fun retrieveFromPlaylistUrl(playlistUrl: String): List<String>? {
        // https://open.spotify.com/playlist/05ey6kjgBaGJvkGh7xyjgq?si=d1000448ca984de2
        val matcher = playlistIdPattern.matcher(playlistUrl)
        if (matcher.matches()) {
            val id = matcher.group(1)
            return getTrackNamesInPlaylist(id)
        }
        return null
    }

    /**
     * Lädt die Lieder von der angegebenen Spotify-URL.
     */
    fun getTrackNamesInPlaylist(playlistId: String): List<String>? {
        synchronized(apiMutex) {
            val playlist = spotifyApi?.getPlaylist(playlistId)
                ?.build()
                ?.execute()
            return playlist?.tracks?.items?.map { it.track.name }
        }
    }

    private fun refreshToken(): Long {
        synchronized(apiMutex) {
            try {
                val credentials = spotifyApi
                    ?.clientCredentials()
                    ?.build()
                    ?.execute()
                spotifyApi?.accessToken = credentials?.accessToken
                if (spotifyApi != null) {
                    // Reset error count
                    renewErrorCount = 0L
                }
                val refreshIn = credentials?.expiresIn?.toLong()
                logger().info("Retrieved token from spotify. Refreshing in $refreshIn s")
                return refreshIn ?: 600L /* Retry after 10 minutes if everything fails */
            } catch (ex: Exception) {
                renewErrorCount++
                // Exponential Backoff
                val retry = (2.5 * 2.0.pow(renewErrorCount.toDouble())).roundToLong()

                logger().error("Error when renewing the spotify token. Consecutive error count: $renewErrorCount. Retry after $retry s", ex)
                return retry
            }
        }
    }
}
