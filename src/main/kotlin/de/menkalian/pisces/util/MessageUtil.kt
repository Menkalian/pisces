package de.menkalian.pisces.util

import de.menkalian.pisces.audio.data.AudioSourceType
import de.menkalian.pisces.audio.data.EPlayTrackResult
import de.menkalian.pisces.audio.data.TrackInfo
import de.menkalian.pisces.message.spec.MessageSpec

/**
 * Fügt ein Feld zu der Nachricht hinzu, das die Informationen aus dem [TrackInfo]-Objekt enthält.
 *
 * @param trackInfo Informationen, die im Feld enthalten sein sollen
 * @param withPosition Wenn dieser Wert `true` ist, wird die aktuelle Position und die Gesamtlänge des Tracks angezeigt.
 *                     Wenn dieser Wert `false` ist, wird nur die Gesamtlänge angezeigt.
 * @return `this` [MessageSpec]-Objekt
 */
fun <T> MessageSpec<T>.addTrackInfoField(trackInfo: TrackInfo, withPosition: Boolean = false): T {
    return addField(
        trackInfo.title,
        """
            Urheber: %s
            %s: `%s%s`
            URI: %s
        """.trimIndent().format(
            trackInfo.author,
            if (withPosition) "Position" else "Länge",
            if (withPosition) trackInfo.position.toDurationString() + "/" else "",
            trackInfo.length.toDurationString(),
            trackInfo.sourceUri
        )
    )
}

/**
 * Formatiert die Nachricht entsprechend des [QueueResult] Objekts.
 *
 * @param queueResult Objekt mit den nötigen Informationen
 * @return `this` [MessageSpec]-Objekt
 */
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
            withTitle("Beim Suchen/Abspielen des Songs ist ein Fehler aufgetreten")
        }
    }
}

/**
 * Setzt die Farbe der Nachricht, um einen Fehler zu symbolisieren.
 *
 * @return `this` [MessageSpec]-Objekt
 */
fun <T> MessageSpec<T>.withErrorColor(): T = withColor(red = 0xff.toByte())

/**
 * Setzt die Farbe der Nachricht, um eine Warnung zu symbolisieren.
 *
 * @return `this` [MessageSpec]-Objekt
 */
fun <T> MessageSpec<T>.withWarningColor(): T = withColor(red = 0xff.toByte(), green = 0x88.toByte())

/**
 * Setzt die Farbe der Nachricht, um einen Erfolg zu symbolisieren.
 *
 * @return `this` [MessageSpec]-Objekt
 */
fun <T> MessageSpec<T>.withSuccessColor(): T = withColor(red = 0x68.toByte(), green = 0xEB.toByte(), blue = 0x27.toByte())

/**
 * Setzt die Farbe der Nachricht auf den Standard zurück.
 *
 * @return `this` [MessageSpec]-Objekt
 */
fun <T> MessageSpec<T>.withDefaultColor(): T = withColor(PiscesColor.colorInt)
