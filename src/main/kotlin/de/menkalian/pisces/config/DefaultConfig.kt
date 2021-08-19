package de.menkalian.pisces.config

import kotlin.reflect.full.memberProperties

class DefaultConfig : IConfig {
    override val featureConfig: FeatureConfig = DefaultFeatureConfig()

    override fun verifyFeatureKey(configKey: String): Boolean {
        try {
            return verifyFeatureKey(featureConfig, configKey.split("."))
        } catch (ex: Exception) {
            return false
        }
    }

    private fun verifyFeatureKey(obj: Any, keys: List<String>): Boolean {
        val clazz = obj::class
        if (keys.isEmpty()) {
            return clazz
                .memberProperties
                .first { it.name == "isEnabled" }
                .call(obj) as Boolean
        } else {
            val child = clazz
                .memberProperties
                .first { it.name == keys.first() }
                .call(obj)!!
            return verifyFeatureKey(child, keys.slice(1 until keys.size))
        }
    }
}