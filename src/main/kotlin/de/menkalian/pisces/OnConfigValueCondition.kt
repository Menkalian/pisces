package de.menkalian.pisces

import de.menkalian.pisces.config.ConfigProvider
import de.menkalian.pisces.util.logger
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * Prüft anhand der Config ob Spring-Beans initialisiert werden sollen.
 */
class OnConfigValueCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        if (metadata.isAnnotated(RequiresKey::class.qualifiedName!!)) {
            val config = ConfigProvider.configInstance
            val configKeys = metadata
                .getAllAnnotationAttributes(RequiresKey::class.qualifiedName!!)
                ?.get("value")
                ?.filterNotNull()
                ?.flatMap { (it as Array<*>).asList() }
                ?.map { it.toString() }
                ?.toList() ?: listOf()

            val activate = configKeys.all(config::verifyFeatureKey)
            logger().debug("Component with necessary features ${configKeys} was ${if (activate) "" else "not "}enabled.")
            return activate
        }
        return false
    }
}
