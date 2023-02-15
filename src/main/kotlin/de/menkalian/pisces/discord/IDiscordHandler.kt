package de.menkalian.pisces.discord

import de.menkalian.pisces.IHandler
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User

interface IDiscordHandler : IHandler {
    /**
     * Informationen zum Account, der bei Discord eingeloggt ist.
     */
    val selfUser: SelfUser

    /**
     * Aktueller Ping zum Discord-Gateway
     */
    val gatewayPing: Long

    /**
     * Aktueller Ping zur Discord-REST-API
     */
    val restPing: Long

    /**
     * Bestimmt den Nutzer, der den aktiven Bot-Account registriert hat.
     */
    fun getOwnerUser() : User

    /**
     * Lädt das JDA-[Guild]-Objekt, das zu der angegebenen ID gehört.
     *
     * @return [Guild]-Objekt mit der angegebenen ID oder `null`, falls keine Guild mit dieser ID bekannt ist.
     */
    fun getJdaGuild(id: Long): Guild?

    /**
     * Lädt das JDA-[User]-Objekt, das zu der angegebenen ID gehört.
     *
     * @return [User]-Objekt mit der angegebenen ID oder `null`, falls kein User mit dieser ID bekannt ist.
     */
    fun getJdaUser(id: Long): User?

    /**
     * Installiert einen Auxiliary-Port auf dem PC und verbindet ein Aux-Kabel damit.
     * Hierdurch wird das Audio direkt von Googles Servern geladen und als Audiosignal in den PC geladen.
     *
     * Hierdurch kommt es zu einer höheren Qualität des Audios bevor es komprimiert wurde, was eine UserExcperience schafft, die optimal für eine Plattform wie Discord ist.
     * Falls kein Aux-Kabel zur Verfügung steht, wird temporär ein CoAx-Glasfaserkabel genutzt bis Amazon das nächste Paket vorbeibringt.
     *
     * Fehler, die bei dieser Methode auftreten können bei `@meitho177#9207` gemeldet werden.
     */
    fun installAux()
}