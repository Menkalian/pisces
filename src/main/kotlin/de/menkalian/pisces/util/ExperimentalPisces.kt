package de.menkalian.pisces.util

/**
 * Annotation für instabile/Work in Progress APIs.
 * Diese APIs können sich noch stark in Verhalten, Aufrufparametern und Funktionsumfang verändern.
 * Sie sollten daher (innerhalb dieses Projektes) nur unter Absprache mit der verantwortlichen Person verwendet werden.
 */
@RequiresOptIn("Unstable API, should not yet be implemented.", RequiresOptIn.Level.WARNING)
annotation class ExperimentalPisces
