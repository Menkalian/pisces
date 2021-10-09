package de.menkalian.pisces.util

import com.wrapper.spotify.SpotifyApi
import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service

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
    final val spotifyApi: SpotifyApi?
    val playlistIdPattern = "https://open.spotify.com/playlist/([a-zA-z0-9]+)\\?.+"
        .toRegex().toPattern()

    init {
        var tmpSpotifyApi: SpotifyApi?
        try {
            tmpSpotifyApi = SpotifyApi.builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build()!!
            val credentials = tmpSpotifyApi
                .clientCredentials()
                .build()
                .execute()
            tmpSpotifyApi.accessToken = credentials.accessToken
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
        val playlist = spotifyApi?.getPlaylist(playlistId)
            ?.build()
            ?.execute()
        return playlist?.tracks?.items?.map { it.track.name }
    }
}
