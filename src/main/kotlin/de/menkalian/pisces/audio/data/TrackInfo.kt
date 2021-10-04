package de.menkalian.pisces.audio.data

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

/**
 * Stellt alle relevanten Informationen zu einem Audio-Track dar.
 *
 * @constructor All-Args Konstruktor der Datenklasse.
 *
 * @property title Titel oder Name des Tracks
 * @property author Bekannter Urheber des Tracks
 * @property state Aktueller Zustand des Tracks
 * @property position Aktuelle Position der Trackwiedergabe
 * @property length Gesamtlänge des Tracks in Millisekunden
 * @property isStream Ob der Track aus einem Stream gelesen wird
 * @property sourcetype Typ der Quelle des Tracks
 * @property sourceIdentifier Quellabhängige Identifikation des Tracks (z.B. Video-ID für [AudioSourceType.YOUTUBE])
 * @property sourceUri Uri der Quelle des Tracks
 */
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
        track.info.title ?: "",
        track.info.author ?: "",
        track.state?.name?.run { AudioTrackState.valueOf(this) } ?: AudioTrackState.UNKNOWN,
        track.position,
        track.duration,
        track.info.isStream,
        AudioSourceType.fromLavaplayerSourceManager(track.sourceManager),
        track.info.identifier ?: "",
        track.info.uri ?: ""
    )
}