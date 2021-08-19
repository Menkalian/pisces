package de.menkalian.pisces.discord

import de.menkalian.pisces.IHandler
import net.dv8tion.jda.api.JDA

interface IDiscordHandler : IHandler {
    val jda: JDA
}