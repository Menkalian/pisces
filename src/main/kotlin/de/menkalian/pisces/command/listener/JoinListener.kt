package de.menkalian.pisces.command.listener

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.audio.IAudioHandler
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.util.logger
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service


@Service
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.JoinListener"])
class JoinListener(val audioHandler: IAudioHandler, val databaseHandler: IDatabaseHandler) : ListenerAdapter() {
    override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
        if (event.member.idLong == event.jda.selfUser.idLong)
            return
        onUserVoiceJoin(
            event.member.idLong,
            event.guild.idLong,
            event.channelJoined.idLong
        )
    }

    override fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
        if (event.member.idLong == event.jda.selfUser.idLong)
            return
        onUserVoiceJoin(
            event.member.idLong,
            event.guild.idLong,
            event.channelJoined.idLong
        )
    }

    private fun onUserVoiceJoin(userId: Long, guildId: Long, channelId: Long) {
        val controller = audioHandler.getGuildAudioController(guildId)

        if (channelId == controller.getConnectedChannel()) {
            logger().debug("Searching Joinsound for user $userId (joined in guild $guildId)")
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