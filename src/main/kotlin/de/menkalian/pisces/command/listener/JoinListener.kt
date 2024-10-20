package de.menkalian.pisces.command.listener

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.audio.IAudioHandler
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.util.logger
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service

/**
 * Listener für die Discord-Events zum Joinen von Personen.
 * Verantwortlich für das Triggern der Joinsounds.
 */
@Service
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.JoinListener"])
class JoinListener(val audioHandler: IAudioHandler, val databaseHandler: IDatabaseHandler) : ListenerAdapter() {

    // TODO: Find replacement events (if any are available ?)
//    override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
//        onUserVoiceJoin(
//            event.member.idLong,
//            event.guild.idLong,
//            event.channelJoined.idLong
//        )
//    }
//
//    override fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
//        onUserVoiceJoin(
//            event.member.idLong,
//            event.guild.idLong,
//            event.channelJoined.idLong
//        )
//    }

    private fun onUserVoiceJoin(userId: Long, guildId: Long, channelId: Long) {
        logger().info("$userId joined $channelId in $guildId")
        val controller = audioHandler.getGuildAudioController(guildId)

        if (channelId == controller.getConnectedChannel() && !controller.togglePause(false)) {
            logger().debug("Searching Joinsound for user $userId")
            val songEntry = databaseHandler.getUserJoinsound(userId)
            logger().debug("Found entry: \"$songEntry\"")
            if (songEntry != null) {
                val result = controller.playTrack(
                    songEntry.url,
                    interruptCurrent = true
                )
                logger().info("Tried to play $songEntry for user $userId in guild $guildId. Result: $result")
            }
        }
    }
}