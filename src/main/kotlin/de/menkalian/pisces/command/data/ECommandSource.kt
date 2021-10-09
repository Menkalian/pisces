package de.menkalian.pisces.command.data

/**
 * Quelle aus der ein Befehl empfangen wurde.
 *
 * @property TEXT Gewöhnlicher Textchat, wo die Nachricht durch ein passendes Präfix als Befehl erkannt wurde.
 * @property COMMAND Ausgelöst durch einen "Slash-Befehl", dem eingebauten Command-System von Discord.
 */
enum class ECommandSource { TEXT, COMMAND }