package de.menkalian.pisces.command.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Datenstruktur für einen **optionalen** zusätzlichen Aufrufparameter.
 * Jedes [ICommand][de.menkalian.pisces.command.ICommand]-Objekt kann beliebig viele Parameter erhalten (auch 0).
 *
 * @property name Name des Parameters. Kann durch das Voranstellen von `--` übergeben werden (z.B. `--name pisces`)
 * @property short Buchstabe als kurze Version des Parameters. Kann durch das Voranstellen von `-` übergeben werden (z.B. `-n pisces`)
 * @property description Beschreibung der Bedeutung/Funktion des Parameters
 * @property type Typ des Parameters
 * @property defaultValue Standardwert des Parameters.
 *                        Der tatsächliche Datentyp hängt von [type] ab und ist in der Dokumentation von [EParameterType] vermerkt.
 *                        Hier **muss** der korrekte Datentyp verwendet werden, sonst werden können unerwartete Werte als Standardwerte geliefert werden.
 *                        Es kommt jedoch **nicht** zu Fehlern/Exceptions.
 * @property currentValue Aktueller tatsächlicher Wert des Parameters. Für den Datentyp gilt das gleiche wie für [defaultValue].
 */
data class CommandParameter(
    val name: String, val short: Char,
    val description: String,
    val type: EParameterType,
    val defaultValue: Any, var currentValue: Any = defaultValue
) {
    /**
     * Interpretiert den aktuellen Wert als [Boolean] und gibt ihn zurück.
     * Falls es Probleme gibt einen Wert zu interpretieren, wird die folgende Reihenfolge verwendet:
     *  - [currentValue] falls gültig, sonst
     *  - [defaultValue] falls gültig, sonst
     *  - `false`
     *
     * @return Aktueller Wert als Wahrheitswert.
     */
    fun asBoolean() = currentValue.toString().toBooleanStrictOrNull() ?: defaultValue.toString().toBooleanStrictOrNull() ?: false

    /**
     * Interpretiert den aktuellen Wert als [Int] und gibt ihn zurück.
     * Falls es Probleme gibt einen Wert zu interpretieren, wird die folgende Reihenfolge verwendet:
     *  - [currentValue] falls gültig, sonst
     *  - [defaultValue] falls gültig, sonst
     *  - `-1`
     *
     * @return Aktueller Wert als Ganzzahl.
     */
    fun asInt() = currentValue.toString().toIntOrNull() ?: defaultValue.toString().toIntOrNull() ?: -1

    /**
     * Interpretiert den aktuellen Wert als [String] und gibt ihn zurück.
     *
     * @return Aktueller Wert als Zeichenkette.
     */
    fun asString() = currentValue.toString()

    /**
     * Interpretiert den aktuellen Wert als [Long] (UserID) und gibt ihn zurück.
     * Falls es Probleme gibt einen Wert zu interpretieren, wird die folgende Reihenfolge verwendet:
     *  - [currentValue] falls gültig, sonst
     *  - [defaultValue] falls gültig, sonst
     *  - `-1L`
     *
     * @return Aktueller Wert als Long ID.
     */
    fun asUserId() = currentValue.toString().toLongOrNull() ?: defaultValue.toString().toLongOrNull() ?: -1L

    /**
     * Interpretiert den aktuellen Wert als [LocalDateTime] und gibt ihn zurück.
     * Falls es Probleme gibt einen Wert zu interpretieren, wird die folgende Reihenfolge verwendet:
     *  - [currentValue] falls gültig, sonst
     *  - [defaultValue] falls gültig, sonst
     *  - [LocalDateTime.MIN] (not null)
     *
     * @return Aktueller Wert als Zeitpunkt.
     */
    fun asTimestamp() = currentValue as? LocalDateTime ?: defaultValue as? LocalDateTime ?: LocalDateTime.MIN!!

    /**
     * Interpretiert den aktuellen Wert als [LocalDate] und gibt ihn zurück.
     * Falls es Probleme gibt einen Wert zu interpretieren, wird die folgende Reihenfolge verwendet:
     *  - [currentValue] falls gültig, sonst
     *  - [defaultValue] falls gültig, sonst
     *  - [LocalDate.MIN] (not null)
     *
     * @return Aktueller Wert als Datum.
     */
    fun asDate() = currentValue as? LocalDate ?: defaultValue as? LocalDate ?: LocalDate.MIN!!

    /**
     * Interpretiert den aktuellen Wert als [LocalTime] und gibt ihn zurück.
     * Falls es Probleme gibt einen Wert zu interpretieren, wird die folgende Reihenfolge verwendet:
     *  - [currentValue] falls gültig, sonst
     *  - [defaultValue] falls gültig, sonst
     *  - [LocalTime.MIN] (not null)
     *
     * @return Aktueller Wert als Uhrzeit.
     */
    fun asTime() = currentValue as? LocalTime ?: defaultValue as? LocalTime ?: LocalTime.MIN!!
}