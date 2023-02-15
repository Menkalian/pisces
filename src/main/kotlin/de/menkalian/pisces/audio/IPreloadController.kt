package de.menkalian.pisces.audio

import de.menkalian.pisces.util.QueueResult

interface IPreloadController {
    /**
     * Lädt einen Audiotrack mit dem angegebenen Suchbegriff vor.
     */
    fun preload(searchterm: String, uuid: String = getUniqueUuid()) : QueueResult

    /**
     * Erzeugt eine einzigartige UUID, die noch keinem vorgeladenen Audio-Track zugewiesen ist.
     */
    fun getUniqueUuid(): String
}