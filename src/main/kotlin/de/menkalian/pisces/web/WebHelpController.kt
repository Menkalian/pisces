package de.menkalian.pisces.web

import de.menkalian.pisces.command.ICommandHandler
import de.menkalian.pisces.web.data.CommandHelpData
import de.menkalian.pisces.web.data.extractHelpData
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class WebHelpController(
    private val commandHandler: ICommandHandler
) {
    @GetMapping("/help/commands")
    fun getCommandList(): Map<String, List<String>> {
        return commandHandler.commands
            .groupBy { it.category }
            .mapValues { it.value.map { it.name } }
    }

    @GetMapping("/help/command/{name}")
    fun getCommandHelp(
        @PathVariable name: String
    ): CommandHelpData {
        val command = commandHandler.getCommand(name)!!
        return extractHelpData(command)
    }
}