package de.menkalian.pisces.web

import de.menkalian.pisces.audio.IAudioHandler
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.util.logger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class WebBuzzerController(
    private val audioHandler: IAudioHandler,
    private val databaseHandler: IDatabaseHandler
) {
    @GetMapping("/buzzer/{id}")
    fun buzz(@PathVariable("id") sid: String) {
        val guildId = sid.toLongOrNull()
        if (guildId != null) {
            logger().info("Buzzer sounded in $sid")
            val controller = audioHandler.getGuildAudioController(guildId)

            if (controller.getConnectedChannel() != null && !controller.togglePause(false)) {
                logger().debug("Searching Buzzer for guild $sid")
                val songEntry = databaseHandler.getGuildBuzzersound(guildId)
                logger().debug("Found entry: \"$songEntry\"")
                if (songEntry != null) {
                    val result = controller.playTrack(
                        songEntry.url,
                        interruptCurrent = true
                    )
                    logger().info("Tried to play $songEntry for guild $guildId. Result: $result")
                }
            }
        }
    }
}