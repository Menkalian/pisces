package de.menkalian.pisces.message

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.discord.IDiscordHandler
import de.menkalian.pisces.util.CommonHandlerImpl
import de.menkalian.pisces.util.asBold
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.message.Handler.JdaMessageHandler"])
class JdaMessageHandler(val discordHandler: IDiscordHandler, val reactionListener: MessageReactionListener) : IMessageHandler, CommonHandlerImpl() {
    override fun initialize() {
        finishInitialization()
    }

    override fun deinitialize() {
        startDeinitialization()
    }

    override fun createMessage(guildId: Long, channelId: Long): MessageBuilder {
        return MessageBuilder(discordHandler, this, guildId, channelId)
    }

    override fun createPrivateMessage(userId: Long): MessageBuilder {
        return MessageBuilder(discordHandler, this, null, userId)
    }

    override fun invalidateMessage(messageInstance: IMessageInstance) {
        reactionListener.removeListeners(messageInstance)
        messageInstance.setColor(red = (255u).toByte())
        messageInstance.addText("\n" + "This message is inactive. Request a new instance.".asBold())
        messageInstance.removeAllReactions()
        messageInstance.stopInvalidationTimer()
    }

    override fun addReactionListener(messageInstance: IMessageInstance, listener: IMessageHandler.IReactionListener) {
        reactionListener.addListener(messageInstance, listener)
    }

    override fun removeReactionListener(messageInstance: IMessageInstance, listener: IMessageHandler.IReactionListener) {
        reactionListener.removeListener(messageInstance, listener)
    }
}