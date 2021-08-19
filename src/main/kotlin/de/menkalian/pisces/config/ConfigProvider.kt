package de.menkalian.pisces.config

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
object ConfigProvider {
    val configInstance: IConfig = DefaultConfig()

    @Bean
    fun getConfig(): IConfig = configInstance
}