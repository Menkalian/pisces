package de.menkalian.pisces.util

import de.menkalian.pisces.IHandler
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Implementiert einen allgemeinen Teil des [IHandler]-Interfaces.
 * Diese Klasse **muss nicht** von jeder [IHandler]-Implementierung verwendet werden, aber in vielen Fällen kann so Code eingespart werden.
 */
abstract class CommonHandlerImpl : IHandler {
    /**
     * Thread-sichere und veränderbare Repräsentation des Initialisierungsstatus.
     */
    private val innerInitialized: AtomicBoolean = AtomicBoolean(false)

    /**
     * Mutex zur Absicherung der (De-)Initialisierung der Komponente
     */
    private val initializationMutex = Any()

    final override val initialized: Boolean
        get() = synchronized(initializationMutex) {
            innerInitialized.get()
        }

    /**
     * Set der registrierten [IHandler.IInitializationHandler]. Eine doppelte Registrierung hat keinen Effekt durch die Nutzung eines [Set]s.
     */
    val initializationHandlers: MutableSet<IHandler.IInitializationHandler> = mutableSetOf()

    override fun addInitializationHandler(handler: IHandler.IInitializationHandler) {
        synchronized(initializationMutex) {
            if (initialized)
                handler.onInitialized(this)
            else
                initializationHandlers.add(handler)
        }
    }

    override fun removeInitializationHandler(handler: IHandler.IInitializationHandler) {
        initializationHandlers.remove(handler)
    }

    /**
     * Führt allgemeine Aufgaben zum Abschließen der Initialisierung durch und ruft die registrierten [IHandler.IInitializationHandler] auf.
     * Sollte am Ende von [IHandler.initialize] aufgerufen werden.
     */
    fun finishInitialization() {
        synchronized(initializationMutex) {
            logger().info("Component \"${this::class.qualifiedName}\" fully initialized.")
            innerInitialized.set(true)
            initializationHandlers
                .forEach { it.onInitialized(this) }
        }
    }

    /**
     * Führt allgemeine Aufgaben zum Abschließen der Deinitialisierung durch und ruft die registrierten [IHandler.IInitializationHandler] auf.
     * Sollte am Anfang von [IHandler.deinitialize] aufgerufen werden.
     */
    fun startDeinitialization() {
        synchronized(initializationMutex) {
            logger().info("Start deinitialization of component \"${this::class.qualifiedName}\".")
            innerInitialized.set(false)
            initializationHandlers
                .forEach { it.onDeinitialized(this) }
        }
    }
}