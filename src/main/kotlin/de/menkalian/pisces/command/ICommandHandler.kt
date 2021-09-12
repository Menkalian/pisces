package de.menkalian.pisces.command

import de.menkalian.pisces.IHandler

/**
 * Der [ICommandHandler] verwaltet die Commands
 */
interface ICommandHandler : IHandler {
    fun execute(cmdString: String)
    fun execute(name: String, parameters: HashMap<String, String>)

    val commandList: List<ICommand>
}