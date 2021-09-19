package de.menkalian.pisces.message.spec

/**
 * Datenklasse zum Speichern von Informationen eines Feldes, das bei der Embedded Message angezeigt wird.
 *
 * @property title Überschrift des Abschnitts
 * @property text Text innerhalb des Abschnitts
 * @property inline Ob das Feld inline angezeigt werden soll (Bis zu 3 inline-Felder können nebeneinander angezeigt werden)
 * @property blank Ob es sich um ein leeres Feld (Platzhalter) handelt oder nicht
 * @property length Berechnete Länge des Feldes, die für die maximal erlaubte Menge von Zeichen bei Discord relevant ist
 */
data class FieldSpec(
    val title: String = "",
    val text: String = "",
    val inline: Boolean,
    val blank: Boolean = false
) { val length = title.length + text.length }