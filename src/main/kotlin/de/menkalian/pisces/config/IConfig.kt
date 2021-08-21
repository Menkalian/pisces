package de.menkalian.pisces.config

/**
 * Schnittstelle für den Zugriff auf die Applikationskonfiguration
 *
 * @property featureConfig FeatureToggle/Implementationseinstellungen. Generierter Code aus der `features.xml`-Datei.
 */
interface IConfig {
    val featureConfig: FeatureConfig

    /**
     * Prüft ob das Feature mit dem angegebenen Schlüssel aktiv ist.
     *
     * @param configKey Schlüssel des gewünschten Features
     *
     * @return `true` wenn das gewünschte Feature existiert und aktiviert ist, sonst `false`
     */
    fun verifyFeatureKey(configKey: String): Boolean
}