package de.menkalian.pisces.util

/**
 * Formatiert den angegebenen String, damit dieser in Discord ```als Codeblock``` angezeigt wird.
 */
fun String.asCodeBlock() = "```$this```"

/**
 * Formatiert den angegebenen String, damit dieser in Discord **fett** angezeigt wird.
 */
fun String.asBold() = "**$this**"

/**
 * Formatiert den angegebenen String, damit dieser in Discord *kursiv* angezeigt wird.
 */
fun String.asItalic() = "*$this*"

/**
 * Formatiert den angegebenen String, damit dieser in Discord ||als Spoiler|| angezeigt wird.
 */
fun String.asSpoiler() = "||$this||"

/**
 * Formatiert den angegebenen String, damit dieser in Discord `als Inline Code` angezeigt wird.
 */
fun String.asInlineCode() = "`$this`"

fun String.shortenTo(maxLength: Int, abbreviationChar: Char = '…'): String {
    if (length <= maxLength)
        return this
    else
        return this.substring(0 until maxLength - 1) + abbreviationChar
}

/**
 * Kürzt den String auf die angegebene Länge.
 * Wenn die Länge überschritten wird, ist das letze Zeichen im String [abbreviationChar].
 *
 * @param maxLength Maximale Länge des Strings
 * @param abbreviationChar Zeichen zur Anzeige, dass eine Kürzung vorgenommen wurde
 * @return gekürzter String
 */
fun String.shortenTo(maxLength: Int, abbreviationChar: Char = '…'): String {
    if (length <= maxLength)
        return this
    else
        return this.substring(0 until maxLength - 1) + abbreviationChar
}

/**
 * Formatiert einen Long-Wert (in Millisekunden) in eine Zeitangabe der Form `HH:MM:SS.mmm`.
 */
fun Long.toDurationString(): String {
    val seconds = this / 1000L
    val minutes = seconds / 60
    val hours = minutes / 60
    return "%02d:%02d:%02d.%03d".format(hours, minutes % 60, seconds % 60, this % 1000L)
}