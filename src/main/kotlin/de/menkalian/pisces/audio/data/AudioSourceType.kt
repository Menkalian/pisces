package de.menkalian.pisces.audio.data

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager
import dev.lavalink.youtube.YoutubeAudioSourceManager
import de.menkalian.pisces.audio.data.AudioSourceType.BANDCAMP
import de.menkalian.pisces.audio.data.AudioSourceType.GETYARN
import de.menkalian.pisces.audio.data.AudioSourceType.HTTP
import de.menkalian.pisces.audio.data.AudioSourceType.LOCAL
import de.menkalian.pisces.audio.data.AudioSourceType.NICO
import de.menkalian.pisces.audio.data.AudioSourceType.SOUNDCLOUD
import de.menkalian.pisces.audio.data.AudioSourceType.TWITCH
import de.menkalian.pisces.audio.data.AudioSourceType.UNKNOWN
import de.menkalian.pisces.audio.data.AudioSourceType.VIMEO
import de.menkalian.pisces.audio.data.AudioSourceType.YOUTUBE

/**
 * Repräsentiert die Quelle des Tracks.
 * Zur Bestimmung dieses Typs wird die Klasse des verwendeten [AudioSourceManager] genutzt.
 *
 * @property BANDCAMP   Audiotrack von `bandcamp.com`
 * @property GETYARN    Audiotrack von `getyarn.io`
 * @property HTTP       Audiotrack der direkt unter einer `http(s)`-URL abgelegt ist.
 * @property LOCAL      Audiotrack der aus einer lokalen Datei gelesen wird.
 * @property NICO       Audiotrack von `nicovideo.jp`
 * @property SOUNDCLOUD Audiotrack von `soundcloud.com`
 * @property TWITCH     Audiotrack von `twitch.tv`
 * @property VIMEO      Audiotrack von `vimeo.com`
 * @property YOUTUBE    Audiotrack von `youtube.com`
 * @property UNKNOWN    Unbekannte Quelle. Falls dieser Wert verwendet wird, hat Lavaplayer vermutlich eine neue Audioquelle hinzugefügt.
 */
enum class AudioSourceType {
    BANDCAMP,
    GETYARN,
    HTTP,
    LOCAL,
    NICO,
    SOUNDCLOUD,
    TWITCH,
    VIMEO,
    YOUTUBE,
    UNKNOWN;

    companion object {
        fun fromLavaplayerSourceManager(manager: AudioSourceManager) = when (manager) {
            is BandcampAudioSourceManager     -> BANDCAMP
            is GetyarnAudioSourceManager      -> GETYARN
            is HttpAudioSourceManager         -> HTTP
            is LocalAudioSourceManager        -> LOCAL
            is NicoAudioSourceManager         -> NICO
            is SoundCloudAudioSourceManager   -> SOUNDCLOUD
            is VimeoAudioSourceManager        -> VIMEO
            is TwitchStreamAudioSourceManager -> TWITCH
            is YoutubeAudioSourceManager      -> YOUTUBE
            else                              -> UNKNOWN
        }
    }
}