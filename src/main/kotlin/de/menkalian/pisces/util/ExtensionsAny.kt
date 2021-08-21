package de.menkalian.pisces.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Stellt für jedes Objekt den korrekten Logger der Klasse bereit.
 */
fun Any.logger(): Logger = LoggerFactory.getLogger(this::class.java)