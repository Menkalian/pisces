package de.menkalian.pisces

/**
 * Grundlegendes Interface für alle Komponenten der Applikation.
 * Jede Komponente kann initialisiert und deinitialisiert werden.
 * Zudem bietet jede Komponente an einen Listener für diese Lifecycle-Events zu registrieren.
 */
interface IHandler {
    /**
     * Flag ob die Komponente initialisiert und einsatzbereit ist
     */
    val initialized: Boolean

    /**
     * Initialisiert die Komponente und ruft die registrierten [IInitializationHandler] auf.
     */
    fun initialize()

    /**
     * Deinitialisiert die Komponente und ruft die registrierten [IInitializationHandler] auf.
     */
    fun deinitialize()

    /**
     * Registriert einen zusätzlichen [IInitializationHandler] für diese Komponente
     */
    fun addInitializationHandler(handler: IInitializationHandler)

    /**
     * Deregistriert den [IInitializationHandler] von dieser Komponente.
     */
    fun removeInitializationHandler(handler: IInitializationHandler)

    /**
     * Handler für die Änderungen am Lifecycle-Status der Komponente
     */
    fun interface IInitializationHandler {
        /**
         * Wird von der Komponente aufgerufen, wenn diese vollständig initialisiert ist.
         */
        fun onInitialized(handler: IHandler)

        /**
         * Wird von der Komponente aufgerufen, bevor die Deinitialisierung beginnt.
         * Nach dem Aufruf dieser Methode darf die Komponente nicht mehr verwendet werden.
         */
        fun onDeinitialized(handler: IHandler) {}
    }
}