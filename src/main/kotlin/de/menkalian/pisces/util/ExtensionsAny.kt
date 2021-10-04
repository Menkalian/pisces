package de.menkalian.pisces.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Stellt für jedes Objekt den korrekten Logger der Klasse bereit.
 * @return Logger, der verwendet werden soll
 */
fun Any.logger(): Logger = LoggerFactory.getLogger(this::class.java)

/**
 * Prüft, ob eine Zahl gerade ist
 * @return true, wenn die Zahl gerade ist
 */
fun Number.isEven() = this.toDouble().mod(2.0) == 0.0

/**
 * Prüft, ob eine Zahl ungerade ist
 * @return true, wenn die Zahl ungerade ist
 */
fun Number.isUneven() = !isEven()