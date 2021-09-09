package de.menkalian.pisces

import java.lang.annotation.Inherited

/**
 * Annotation zum Festlegen der Features, die in der Config aktiv sein müssen, um diese Komponente zu aktivieren.
 * Diese Annotation sollte gemeinsam mit `@Conditional(OnConfigValueCondition::class)` genutzt werden.
 *
 * @see OnConfigValueCondition
 */
@Repeatable
@Inherited
annotation class RequiresKey(val value: Array<String>)