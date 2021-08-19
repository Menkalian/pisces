package de.menkalian.pisces

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Implements some common code for the {@link IHandler}-interface.
 * This is separated to have the original interface as clean as possible.
 */
interface IHandlerCommonImpl : IHandler {
    val innerInitialized: AtomicBoolean
    override val initialized: Boolean
        get() = innerInitialized.get()

    val initializationHandlers: MutableSet<IHandler.IInitializationHandler>
    override fun addInitializationHandler(handler: IHandler.IInitializationHandler) {
        synchronized(initializationHandlers) {
            if (initialized)
                handler.onInitialized(this)
            else
                initializationHandlers.add(handler)
        }
    }

    override fun removeInitializationHandler(handler: IHandler.IInitializationHandler) {
        initializationHandlers.remove(handler)
    }

    fun finishInitialization() {
        synchronized(initializationHandlers) {
            innerInitialized.set(true)
            initializationHandlers
                .forEach { it.onInitialized(this) }
        }
    }
}