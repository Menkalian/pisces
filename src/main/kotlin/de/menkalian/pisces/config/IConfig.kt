package de.menkalian.pisces.config

interface IConfig {
    val featureConfig: FeatureConfig

    fun verifyFeatureKey(configKey: String): Boolean
}