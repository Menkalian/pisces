package de.menkalian.pisces.message.spec

/**
 * Datenklasse zum Speichern der Autoreninformationen, die bei der Embedded Message angezeigt werden.
 *
 * @property name Angezeigter Name
 * @property url Url auf die durch die Nachricht verlinkt wird
 * @property iconUrl Url des Profilbildes, das für den Autor angezeigt werden soll.
 */
data class AuthorSpec(
    val name: String,
    val url: String,
    val iconUrl: String
)