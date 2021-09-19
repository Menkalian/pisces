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