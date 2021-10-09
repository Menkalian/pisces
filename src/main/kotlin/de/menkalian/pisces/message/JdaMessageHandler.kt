package de.menkalian.pisces.message

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.discord.IDiscordHandler
import de.menkalian.pisces.util.CommonHandlerImpl
import de.menkalian.pisces.util.asBold
import de.menkalian.pisces.util.logger
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

/**
 * JDA Standardimplementierung der [IMessageHandler]-Schnittstelle.
 */
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.message.Handler.JdaMessageHandler"])
class JdaMessageHandler(val beanFactory: BeanFactory, val reactionListener: MessageReactionListener) : IMessageHandler, CommonHandlerImpl() {
    private lateinit var discordHandler: IDiscordHandler
    override fun initialize() {
        discordHandler = beanFactory.getBean(IDiscordHandler::class.java)
        finishInitialization()
    }

    override fun deinitialize() {
        startDeinitialization()
    }

    override fun createMessage(guildId: Long, channelId: Long): MessageBuilder {
        logger().info("Creating new message to Channel $guildId->$channelId")
        return MessageBuilder(discordHandler, this, guildId, channelId)
    }

    override fun createPrivateMessage(userId: Long): MessageBuilder {
        logger().info("Creating new message to User $userId")
        return MessageBuilder(discordHandler, this, null, userId)
    }

    override fun invalidateMessage(messageInstance: IMessageInstance) {
        logger().debug("Invalidating $messageInstance")
        clearAllReactionListeners(messageInstance)
        messageInstance.setColor(red = (255u).toByte())
        messageInstance.addText("\n" + "This message is inactive. Request a new instance.".asBold())
        messageInstance.removeAllReactions()
        messageInstance.stopInvalidationTimer()
    }

    override fun clearAllReactionListeners(messageInstance: IMessageInstance) {
        reactionListener.removeListeners(messageInstance)
    }

    override fun addReactionListener(messageInstance: IMessageInstance, listener: IMessageHandler.IReactionListener) {
        reactionListener.addListener(messageInstance, listener)
    }

    override fun removeReactionListener(messageInstance: IMessageInstance, listener: IMessageHandler.IReactionListener) {
        reactionListener.removeListener(messageInstance, listener)
    }
}