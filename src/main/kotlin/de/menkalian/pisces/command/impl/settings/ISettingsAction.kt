package de.menkalian.pisces.command.impl.settings

/**
 * Schnittstelle zur Darstellung der Aktionen, die für eine Einstellung vorgenommen werden können
 */
interface ISettingsAction {
    /**
     * Liest den aktuellen Wert der Einstellung aus und gibt diesen Wert zurück
     */
    fun get(): String

    /**
     * Informiert den Nutzer über den akutellen Wert der Einstellung
     */
    fun notifyCurrent()

    /**
     * Ändert den aktuellen Wert der Einstellung und benachrichtigt den Nutzer zu dieser Änderung
     */
    fun set(value: String)
}