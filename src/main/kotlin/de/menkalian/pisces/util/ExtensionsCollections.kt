package de.menkalian.pisces.util

fun <T> List<T>.shuffleIf(condition: Boolean) = if (condition) shuffled() else this