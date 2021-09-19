package de.menkalian.pisces.util

import de.menkalian.pisces.audio.data.EPlayTrackResult
import de.menkalian.pisces.audio.data.TrackInfo
import java.awt.Color
import java.nio.ByteBuffer

/**
 * Kombination aus dem Lade/Suchergebnis eines Tracks und den Informationen zu dem/den geladenen Track(s).
 */
typealias QueueResult = Pair<EPlayTrackResult, List<TrackInfo>>

typealias Variables = MutableMap<String, String>
typealias FixedVariables = Map<String, String>

object PiscesColor {
    val red: UByte = 121u
    val green: UByte = 170u
    val blue: UByte = 247u

    val colorInt: Int
        get() = ByteBuffer.allocate(4)
            .put(0x1F)
            .put(red.toByte())
            .put(green.toByte())
            .put(blue.toByte())
            .getInt(0)

    val colorAwt: Color
        get() = Color(colorInt)
}
