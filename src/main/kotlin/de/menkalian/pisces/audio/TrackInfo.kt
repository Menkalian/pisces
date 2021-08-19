package de.menkalian.pisces.audio

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

data class TrackInfo(
    val title: String,
    val author: String,

    val state: AudioTrackState,
    val position: Long,
    val length: Long,
    val isStream: Boolean,

    val sourcetype: AudioSourceType,
    val sourceIdentifier: String,
    val sourceUri: String
) {
    constructor(track: AudioTrack) : this(
        track.info.title,
        track.info.author,
        AudioTrackState.valueOf(track.state.name),
        track.position,
        track.duration,
        track.info.isStream,
        AudioSourceType.fromLavaplayerSourceManager(track.sourceManager),
        track.info.identifier,
        track.info.uri
    )
}

enum class AudioSourceType {
    BANDCAMP,
    BEAM,
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
            is BeamAudioSourceManager         -> BEAM
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

enum class AudioTrackState {
    INACTIVE, LOADING, PLAYING, SEEKING, STOPPING, FINISHED
}

