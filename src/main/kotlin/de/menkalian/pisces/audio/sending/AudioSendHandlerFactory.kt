package de.menkalian.pisces.audio.sending

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import de.menkalian.pisces.config.IConfig
import de.menkalian.pisces.util.logger
import org.springframework.stereotype.Service

/**
 * Factory zur Bereitstellung von [IExtendedAudioSendHandler]-Instanzen.
 * Die genaue Implementierung der Instanzen kann über die Konfiguration festgelegt werden.
 * Eine Instanz dieser Klasse ist als Spring-Bean verfügbar.
 *
 * @constructor Standardkonstruktor. Wird von Spring zum Instanziieren genutzt.
 * @param config Aktuelle Konfiguration der Applikation
 */
@Service
class AudioSendHandlerFactory(val config: IConfig) {

    /**
     * Erstellt eine [IExtendedAudioSendHandler]-Instanz, abhängig von der aktuellen Konfiguration und den Eingabeparametern.
     *
     * @param player [AudioPlayer]-Instanz, die von dem SendHandler genutzt werden soll.
     * @return Erstellte Instanz
     *
     * @throws IllegalArgumentException Wenn die aktuelle Konfiguration keine gültige SendHandler-Implementierung gesetzt hat oder AudioSendHandler deaktiviert sind.
     */
    fun createAudioPlayerSendHandler(player: AudioPlayer): IExtendedAudioSendHandler {
        logger().info("Creating LavaplayerAudioSendHandler with player {$player}")

        if(config.featureConfig.pisces.audio.ExtendedAudioSendHandler.isEnabled.not())
            throw IllegalArgumentException("ExtendedAudioPlayerSendHandler inactive in configuration")

        return config.featureConfig.pisces.audio.ExtendedAudioSendHandler.run {
            when(activeImplementation) {
                UnfilteredLavaplayerAudioSendHandler -> UnfilteredLavaplayerAudioSendHandler(player)
                else -> throw IllegalArgumentException("Active configuration unsupported: ${activeImplementation::class.simpleName}")
            }
        }
    }
}