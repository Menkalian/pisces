package de.menkalian.pisces.web.data

import de.menkalian.pisces.command.ICommand

data class CommandHelpData(
    val name: String,
    val description: String,
    val category: String,
    val parameters: List<ParameterHelpData>
)

data class ParameterHelpData(
    val name: String,
    val short: String,
    val description: String,
    val type: String,
    val defaultValue: String
)

fun extractHelpData(command: ICommand): CommandHelpData {
    return CommandHelpData(
        command.name,
        command.description,
        command.category,
        command.parameters.map {
            ParameterHelpData(
                it.name,
                it.short.toString(),
                it.description,
                it.type.name,
                it.defaultValue.toString()
            )
        }
    )
}
