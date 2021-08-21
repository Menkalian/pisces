package de.menkalian.pisces

import de.menkalian.pisces.config.ConfigProvider
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class OnConfigValueCondition : Condition {
    val log = LoggerFactory.getLogger(this::class.java)!!

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
            log.debug("Component with necessary features ${configKeys} was ${if (activate) "" else "not "}enabled.")
            return activate
        }
        return false
    }
}
