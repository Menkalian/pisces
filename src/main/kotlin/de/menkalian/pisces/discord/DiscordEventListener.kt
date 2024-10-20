package de.menkalian.pisces.discord

import de.menkalian.pisces.util.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.ExceptionEvent
import net.dv8tion.jda.api.events.StatusChangeEvent
import net.dv8tion.jda.api.events.session.*
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess

@Service
class DiscordEventListener : ListenerAdapter() {

    override fun onSessionInvalidate(event: SessionInvalidateEvent) {
        logger().warn("Session was invalidated. Restarting...")
        event.jda.shutdown()
        exitProcess(-0x1001)
    }

    override fun onSessionDisconnect(event: SessionDisconnectEvent) {
        logger().warn("Connection to the discord api-gateway was lost. Let's just wait a moment.")
    }

    override fun onSessionResume(event: SessionResumeEvent) {
        logger().info("Resumed connection to discord.")
        sendOwnerText(event.jda, "Resumed connection to discord.")
    }

    override fun onSessionRecreate(event: SessionRecreateEvent) {
        logger().info("Reconnected to discord (session recreated).")
        sendOwnerText(event.jda, "Reconnected to discord. The previous connection could not be resumed.")
    }

    override fun onShutdown(event: ShutdownEvent) {
        logger().info("Shutting down JDA.")
    }

    override fun onReady(event: ReadyEvent) {
        logger().info("Connection established. JDA is ready.")
        sendOwnerText(event.jda, "Pisces-Bot (${event.jda.selfUser.name}) wurde gestartet. ${Emoji.ANIMAL_WHALE2}")
    }

    override fun onStatusChange(event: StatusChangeEvent) {
        logger().debug("JDA-Status changed: ${event.oldStatus} -> ${event.newStatus}")
    }

    override fun onException(event: ExceptionEvent) {
        logger().error("An uncaught exception occured", event.cause)

        val stringDataOutputStream = ByteArrayOutputStream()
        val printStream = PrintStream(stringDataOutputStream)
        event.cause.printStackTrace(printStream)
        printStream.close()

        sendOwnerText(event.jda, "An exception occured: ${event.cause.message}")
        sendOwnerText(
            event.jda,
            "StackTrace:\n${stringDataOutputStream.toString(StandardCharsets.UTF_8)}"
                .take(Message.MAX_CONTENT_LENGTH - 6)
                .asCodeBlock()
        )
    }

    private fun sendOwnerText(jda: JDA, text: String) {
        jda.retrieveApplicationInfo().complete()
            .owner.openPrivateChannel().complete()
            .sendMessage(text).queue()
    }
}