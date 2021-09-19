package de.menkalian.pisces.util

fun String.asCodeBlock() = "```$this```"
fun String.asBold() = "**$this**"
fun String.asItalic() = "*$this*"
fun String.asSpoiler() = "||$this||"
fun String.asInlineCode() = "`$this`"