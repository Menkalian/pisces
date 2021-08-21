package de.menkalian.pisces.config

import de.menkalian.pisces.config.ConfigProvider.configInstance
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

/**
 * Stellt die aktuelle Konfiguration bereit.
 * Diese Bereitstellung erfolgt sowohl als `Spring`-Bean, als auch als Singleton-Objekt.
 * In beiden Fällen handelt es sich um die gleiche Instanz der Konfiguration.
 *
 * @property configInstance Einmalige Instanz der Konfiguration.
 */
@Component
object ConfigProvider {
    val configInstance: IConfig = DefaultConfig()

    /**
     * Methode zur Bereitstellung der `Spring`-Bean für die Konfiguration.
     *
     * @return Einmalige Instanz der Konfiguration
     */
    @Bean
    fun getConfig(): IConfig = configInstance
}