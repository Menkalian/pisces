package de.menkalian.pisces.util

import de.menkalian.pisces.audio.data.AudioSourceType
import de.menkalian.pisces.audio.data.EPlayTrackResult
import de.menkalian.pisces.message.spec.MessageSpec

fun <T> MessageSpec<T>.applyQueueResult(queueResult: QueueResult): T {
    queueResult.second.forEach {
        addField(
            it.title,
            """
                Urheber: %s
                Länge:   %s
                URI:     %s
            """.trimIndent()
                .format(
                    it.author,
                    it.length.toDurationString(),
                    it.sourceUri
                )
        )
    }

    if (queueResult.second.size == 1) {
        val singleSong = queueResult.second.first()
        if (singleSong.sourcetype == AudioSourceType.YOUTUBE) {
            withThumbnail("https://img.youtube.com/vi/${singleSong.sourceIdentifier}/default.jpg")
        }
    }

    return when (queueResult.first) {
        EPlayTrackResult.TRACK_URL           -> {
            withColor(red = 104.toByte(), green = 232.toByte(), blue = 39.toByte())
            withTitle("Der Song wurde von der angegebenen URL geladen")
        }
        EPlayTrackResult.TRACK_SEARCH        -> {
            withColor(red = 104.toByte(), green = 232.toByte(), blue = 39.toByte())
            withTitle("Der Song wurde auf Youtube gesucht und gefunden")
        }
        EPlayTrackResult.PLAYLIST            -> {
            withColor(red = 104.toByte(), green = 232.toByte(), blue = 39.toByte())
            withTitle("Die Playlist wurde erkannt und erfolgreich geladen")
        }
        EPlayTrackResult.TRACK_FROM_PLAYLIST -> {
            withColor(red = 104.toByte(), green = 232.toByte(), blue = 39.toByte())
            withTitle("Der Song wurde aus der Playlist geladen")
        }
        EPlayTrackResult.NOT_FOUND           -> {
            withColor(red = 255.toByte(), green = 136.toByte())
            withTitle("Es konnte kein Song gefunden werden")
        }
        EPlayTrackResult.ERROR               -> {
            withColor(red = 255.toByte(), green = 40.toByte())
            withText("Beim Suchen/Abspielen des Songs ist ein Fehler aufgetreten")
        }
    }
}