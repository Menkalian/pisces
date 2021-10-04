package de.menkalian.pisces.discord

import de.menkalian.pisces.IHandler
import net.dv8tion.jda.api.JDA

interface IDiscordHandler : IHandler {
    //TODO: Refactor IDiscordHandler (to make it independent from JDA)
    val jda: JDA
}