package de.menkalian.pisces.util

import de.menkalian.pisces.audio.data.AudioSourceType
import de.menkalian.pisces.audio.data.EPlayTrackResult
import de.menkalian.pisces.audio.data.TrackInfo
import de.menkalian.pisces.message.spec.MessageSpec

fun <T> MessageSpec<T>.addTrackInfoField(trackInfo: TrackInfo): T {
    return addField(
        trackInfo.title,
        """
                Urheber: %s
                Länge:   %s
                URI:     %s
            """.trimIndent()
            .format(
                trackInfo.author,
                trackInfo.length.toDurationString(),
                trackInfo.sourceUri
            )
    )
}


fun <T> MessageSpec<T>.applyQueueResult(queueResult: QueueResult): T {
    queueResult.second.forEach {
        addTrackInfoField(it)
    }

    if (queueResult.second.size == 1) {
        val singleSong = queueResult.second.first()
        if (singleSong.sourcetype == AudioSourceType.YOUTUBE) {
            withThumbnail("https://img.youtube.com/vi/${singleSong.sourceIdentifier}/default.jpg")
        }
    }

    return when (queueResult.first) {
        EPlayTrackResult.TRACK_URL           -> {
            withSuccessColor()
            withTitle("Der Song wurde von der angegebenen URL geladen")
        }
        EPlayTrackResult.TRACK_SEARCH        -> {
            withSuccessColor()
            withTitle("Der Song wurde auf Youtube gesucht und gefunden")
        }
        EPlayTrackResult.PLAYLIST            -> {
            withSuccessColor()
            withTitle("Die Playlist wurde erkannt und erfolgreich geladen")
        }
        EPlayTrackResult.TRACK_FROM_PLAYLIST -> {
            withSuccessColor()
            withTitle("Der Song wurde aus der Playlist geladen")
        }
        EPlayTrackResult.NOT_FOUND           -> {
            withWarningColor()
            withTitle("Es konnte kein Song gefunden werden")
        }
        EPlayTrackResult.ERROR               -> {
            withErrorColor()
            withText("Beim Suchen/Abspielen des Songs ist ein Fehler aufgetreten")
        }
    }
}

fun <T> MessageSpec<T>.withErrorColor(): T = withColor(red = 0xff.toByte())
fun <T> MessageSpec<T>.withWarningColor(): T = withColor(red = 0xff.toByte(), green = 0x88.toByte())
fun <T> MessageSpec<T>.withSuccessColor(): T = withColor(red = 0x68.toByte(), green = 0xEB.toByte(), blue = 0x27.toByte())
fun <T> MessageSpec<T>.withDefaultColor(): T = withColor(PiscesColor.colorInt)
