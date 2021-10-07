package de.menkalian.pisces.discord

/**
 * Enthält die relevanten Daten zum Bot-User, der aktuell bei Discord angemeldet ist.
 *
 * @property id Discord-ID des Accounts
 * @property name Accountname
 * @property avatarUrl URL zum Profilbild des Accounts
 */
data class SelfUser(
    val id: Long,
    val name: String,
    val avatarUrl: String
)
