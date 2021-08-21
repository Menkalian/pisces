package de.menkalian.pisces

interface IHandler {
    val initialized: Boolean

    fun initialize()
    fun deinitialize()

    fun addInitializationHandler(handler: IInitializationHandler)
    fun removeInitializationHandler(handler: IInitializationHandler)

    fun interface IInitializationHandler {
        fun onInitialized(handler: IHandler)
        fun onDeinitialized(handler: IHandler) {}
    }
}