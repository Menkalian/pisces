package de.menkalian.pisces.command.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class CommandParameter(
    val name: String, val short: Char,
    val description: String,
    val type: EParameterType,
    val defaultValue: Any, var currentValue: Any = defaultValue
) {
    fun asInt() = currentValue.toString().toIntOrNull() ?: defaultValue.toString().toInt()
    fun asString() = currentValue.toString()
    fun asUserId() = currentValue.toString().toLongOrNull() ?: -1L
    fun asTimestamp() = currentValue as? LocalDateTime ?: defaultValue as? LocalDateTime ?: LocalDateTime.MIN!!
    fun asDate() = currentValue as? LocalDate ?: defaultValue as? LocalDate ?: LocalDate.MIN!!
    fun asTime() = currentValue as? LocalTime ?: defaultValue as? LocalTime ?: LocalTime.MIN!!
}