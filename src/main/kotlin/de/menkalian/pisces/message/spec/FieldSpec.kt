package de.menkalian.pisces.message.spec

data class FieldSpec(
    val title: String = "",
    val text: String = "",
    val inline: Boolean,
    val blank: Boolean = false
) { val length = title.length + text.length }