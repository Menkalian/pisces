package de.menkalian.pisces.util

import de.menkalian.pisces.audio.data.EPlayTrackResult
import de.menkalian.pisces.audio.data.TrackInfo
import de.menkalian.pisces.util.PiscesColor.colorAwt
import de.menkalian.pisces.util.PiscesColor.colorInt
import de.menkalian.pisces.util.PiscesColor.red
import java.awt.Color
import java.nio.ByteBuffer

/**
 * Kombination aus dem Lade/Suchergebnis eines Tracks und den Informationen zu dem/den geladenen Track(s).
 */
typealias QueueResult = Pair<EPlayTrackResult, List<TrackInfo>>

/**
 * Typenalias für eine Struktur zum Abspeichern von (frei wählbaren) Zusatzwerten in Form von Schlüssel-Wert-Paaren.
 * Die Werte dieser Struktur sind veränderlich.
 * Es handelt sich hierbei um eine Unterklasse von [FixedVariables].
 *
 * @see [FixedVariables]
 */
typealias Variables = MutableMap<String, String>
/**
 * Typenalias für eine Struktur zum Abspeichern von (frei wählbaren) Zusatzwerten in Form von Schlüssel-Wert-Paaren.
 * Die Werte dieser Struktur können nicht verändert werden.
 *
 * @see [Variables]
 */
typealias FixedVariables = Map<String, String>

/**
 * Objekt zum Speichern von Informationen zur zentralen Farbe des "Pisces"-Projektes.
 *
 * @property red Bytewert (0-255) der roten Farbkomponente
 * @property red Bytewert (0-255) der grünen Farbkomponente
 * @property red Bytewert (0-255) der blauen Farbkomponente
 * @property colorInt Zusammengesetzter RGB-Farbwert als Zahlenwert. Bytefolge {`1F`, [Rot][PiscesColor.red], [Grün][PiscesColor.green], [Blau][PiscesColor.blue]}
 * @property colorAwt Farbe als [Color]-Objekt
 */
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
