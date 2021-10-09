package de.menkalian.pisces.command.data

/**
 * Datentyp eines Parameters.
 * Dieser Wert legt fest was bei [CommandParameter.defaultValue] als Datentyp erwartet wird.
 *
 * @property BOOLEAN Wahrheitswert, repräsentiert durch [Boolean]
 * @property INTEGER Ganzzahl, repräsentiert durch [Integer]
 * @property STRING  Text, repräsentiert durch [String]
 * @property USER    Nutzer-ID, repräsentiert durch [Long]
 * @property TIMESTAMP Zeitpunkt, repräsentiert durch [java.time.LocalDateTime]
 * @property DATE Datum, repräsentiert durch [java.time.LocalDate]
 * @property TIME Uhrzeit, repräsentiert durch [java.time.LocalTime]
 */
enum class EParameterType { BOOLEAN, INTEGER, STRING, USER, TIMESTAMP, DATE, TIME }