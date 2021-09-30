package de.menkalian.pisces.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist
import de.menkalian.pisces.audio.data.EPlayTrackResult
import de.menkalian.pisces.audio.data.TrackInfo
import de.menkalian.pisces.audio.queue.TrackQueue
import de.menkalian.pisces.audio.sending.AudioSendHandlerFactory
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.discord.IDiscordHandler
import de.menkalian.pisces.util.QueueResult
import de.menkalian.pisces.util.logger
import de.menkalian.pisces.variables.FlunderKey.Flunder
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.managers.AudioManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Standardimplementierung von [IGuildAudioController] bei der Verwendung von JDA + Lavaplayer als Discord-Wrapper.
 *
 * @property guildId Discord ID des Servers ([Guild][net.dv8tion.jda.api.entities.Guild]) für den die Instanz des [JdaGuildAudioController]s angelegt wurde.
 * @property sendHandlerFactory Factory zur Erstellung von SendHandler-Instanzen
 *
 * @property connectionLock Mutex für den Zugriff auf die Audioverbindung
 * @property playerAndQueueLock Mutex für den Zugriff auf die Queue und den AudioPlayer
 *
 * @property player Verwendete Lavaplayer-Instanz
 * @property jdaGuildAudioManager AudioManager des Servers ([Guild][net.dv8tion.jda.api.entities.Guild])
 * @property trackQueue Queue für die AudioTracks
 *
 * @property isShuffle Wert zum Speichern, ob der nächste Track zufällig aus der Liste ausgewählt werden soll.
 * @property isRepeat Wert zum Speichern, ob der nächste Track zufällig aus der Liste ausgewählt werden soll.
 */
class JdaGuildAudioController(
    val guildId: Long, private val sendHandlerFactory: AudioSendHandlerFactory,
    private val playerManager: AudioPlayerManager, discordHandler: IDiscordHandler,
    databaseHandler: IDatabaseHandler
) : IGuildAudioController, AudioEventListener, AudioEventAdapter() {

    // Synchronisation Locks
    private val connectionLock = Any()
    private val playerAndQueueLock = Any()

    // Controlled objects
    private val player: AudioPlayer
    private val jdaGuildAudioManager: AudioManager
    private val trackQueue = TrackQueue()
    private val interruptedStack = ArrayDeque<AudioTrack>(5)

    // changeable values
    private val isShuffle = AtomicBoolean(false)
    private val isRepeat = AtomicBoolean(false)

    init {
        this.logger().debug("Initializing $this")
        this.logger().trace("Initializing AudioPlayer")
        player = playerManager.createPlayer()!!

        this.logger().trace("Configuring AudioPlayer")
        player.addListener(this)

        this.logger().trace("AudioPlayer setup complete.")

        this.logger().debug("Setting up JDA GuildAudioManager")
        jdaGuildAudioManager = discordHandler.jda
            .getGuildById(guildId)
            ?.audioManager ?: throw IllegalArgumentException("Invalid GuildId")
        jdaGuildAudioManager.sendingHandler = createSendingHandler(player)

        this.logger().debug("Loading default settings for $guildId.")
        isShuffle.set(databaseHandler.getSettingsValue(guildId, Flunder.Guild.Settings.Shuffle.toString()).toBooleanStrictOrNull() ?: false)
        isRepeat.set(databaseHandler.getSettingsValue(guildId, Flunder.Guild.Settings.Repeat.toString()).toBooleanStrictOrNull() ?: false)

        this.logger().info("Initialization of $this complete.")
    }

    override fun getConnectedChannel(): Long? =
        synchronized(connectionLock) {
            jdaGuildAudioManager.guild.audioManager.connectedChannel?.idLong
        }

    override fun getUserVoiceChannelId(userId: Long): Long? {
        return jdaGuildAudioManager.guild
            .getMemberById(userId)
            ?.voiceState
            ?.channel
            ?.idLong
    }

    override fun connect(channelId: Long): Boolean {
        synchronized(connectionLock) {
            val channel = jdaGuildAudioManager.guild.getGuildChannelById(channelId)
            return if (channel is VoiceChannel) {
                try {
                    logger().debug("Try opening audio connection to $channel")
                    jdaGuildAudioManager.openAudioConnection(channel)
                    logger().info("$this connected to $channel")
                    true
                } catch (ex: Exception) {
                    logger().error("$this could not open a connection to channel $channelId (${channel.name})", ex)
                    false
                }
            } else {
                logger().error("Target $channelId is no valid VoiceChannel.")
                false
            }
        }
    }

    override fun disconnect(): Boolean {
        synchronized(connectionLock) {
            val previousChannel = jdaGuildAudioManager.connectedChannel
            logger().debug("Disconnecting from $previousChannel")
            jdaGuildAudioManager.closeAudioConnection()
            return previousChannel != null
        }
    }

    override fun reset() {
        synchronized(playerAndQueueLock) {
            player.destroy()
            player.isPaused = false
            trackQueue.clear()
            interruptedStack.clear()
            isShuffle.set(false)
            isRepeat.set(false)
        }
    }

    override fun stop() {
        synchronized(playerAndQueueLock) {
            player.destroy()
        }
    }

    override fun playTrack(searchterm: String, playInstant: Boolean, interruptCurrent: Boolean, playFullPlaylist: Boolean): QueueResult {
        logger().info("Searching for \"$searchterm\" and queuing it afterwards (instant=$playInstant, interrupt=$interruptCurrent, fullPlaylist=$playFullPlaylist)")
        val completable = CompletableFuture<QueueResult>()

        playerManager.loadItemOrdered(this, searchterm, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack?) {
                logger().debug("$this found track for \"$searchterm\": ${track?.makeInfo()}")
                if (track != null) {
                    addTrack(track, playInstant = playInstant, interruptCurrent = interruptCurrent)
                    completable.complete(QueueResult(EPlayTrackResult.TRACK_URL, listOf(track.makeInfo())))
                } else {
                    // Try as fallback if invalid data is returned
                    noMatches()
                }
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {
                logger().debug("$this found playlist for \"$searchterm\": {name = ${playlist?.name}; ${playlist?.tracks?.map { it.makeInfo() }}")
                if (playlist == null) {
                    // Try as fallback if invalid data is returned
                    noMatches()
                    return
                }

                if (playlist.tracks.isEmpty()) {
                    completable.complete(QueueResult(EPlayTrackResult.PLAYLIST, listOf()))
                    return
                }

                if (playFullPlaylist) {
                    val tracks = playlist.tracks
                    logger().debug("$this is playing all tracks from ${playlist.name}")

                    tracks.forEachIndexed { index, it ->
                        // Only add the first track with the flags
                        addTrack(it, playInstant = playInstant && index == 0, interruptCurrent = interruptCurrent && index == 0)
                    }

                    completable.complete(QueueResult(EPlayTrackResult.PLAYLIST, tracks.map { it.makeInfo() }))
                } else {
                    val selectedTrack = playlist.selectedTrack
                    logger().debug("$this found track in playlist ${playlist.name}: ${selectedTrack?.makeInfo()}")
                    addTrack(selectedTrack, playInstant = playInstant, interruptCurrent = interruptCurrent)
                    completable.complete(QueueResult(EPlayTrackResult.TRACK_FROM_PLAYLIST, listOf(selectedTrack.makeInfo())))
                }
            }

            override fun noMatches() {
                logger().debug("$this found no matches for \"$searchterm\". Searching on Youtube...")
                val searchResult = searchYoutube(searchterm)
                if (searchResult.isNotEmpty()) {
                    val trackToAdd = searchResult.first()
                    logger().debug("Using ${trackToAdd.makeInfo()} and adding it to the queue")
                    addTrack(trackToAdd, playInstant = playInstant, interruptCurrent = interruptCurrent)
                    completable.complete(QueueResult(EPlayTrackResult.TRACK_SEARCH, listOf(trackToAdd.makeInfo())))
                } else {
                    completable.complete(QueueResult(EPlayTrackResult.NOT_FOUND, listOf()))
                }
            }

            override fun loadFailed(exception: FriendlyException?) {
                logger().error("$this failed to load \"$searchterm\".", exception)
                completable.complete(QueueResult(EPlayTrackResult.ERROR, listOf()))
            }
        })

        return completable.get()
    }

    override fun lookupTracks(searchterm: String, enableSearch: Boolean, results: Int): QueueResult {
        logger().info("Looking up \"$searchterm\" (enableSearch=$enableSearch, maxResults=$results)")
        val completable = CompletableFuture<QueueResult>()

        playerManager.loadItem(searchterm, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack?) {
                logger().debug("$this found track for \"$searchterm\": ${track?.makeInfo()}")
                if (track != null) {
                    completable.complete(QueueResult(EPlayTrackResult.TRACK_URL, listOf(track.makeInfo())))
                } else {
                    // Try as fallback if invalid data is returned
                    noMatches()
                }
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {
                logger().debug("$this found playlist for \"$searchterm\": {name = ${playlist?.name}; ${playlist?.tracks?.map { it.makeInfo() }}")
                if (playlist == null) {
                    // Try as fallback if invalid data is returned
                    noMatches()
                    return
                }

                val result =
                    if (playlist.tracks.isEmpty())
                        EPlayTrackResult.NOT_FOUND
                    else
                        EPlayTrackResult.PLAYLIST
                completable.complete(QueueResult(result, playlist.tracks.map { it.makeInfo() }))
            }

            override fun noMatches() {
                if (enableSearch) {
                    logger().debug("$this found no matches for \"$searchterm\". Searching on Youtube...")
                    val searchResult = searchYoutube(searchterm)
                    if (searchResult.isNotEmpty()) {
                        completable.complete(QueueResult(EPlayTrackResult.TRACK_SEARCH, searchResult.take(results).map { it.makeInfo() }))
                        return
                    }
                }
                completable.complete(QueueResult(EPlayTrackResult.NOT_FOUND, listOf()))
            }

            override fun loadFailed(exception: FriendlyException?) {
                logger().error("$this failed to load \"$searchterm\".", exception)
                completable.complete(QueueResult(EPlayTrackResult.ERROR, listOf()))
            }
        })

        return completable.get()
    }

    override fun skipTracks(requeue: Boolean, skipAmount: Int): List<TrackInfo> {
        if (skipAmount < 1)
            return listOf()

        return synchronized(playerAndQueueLock) {
            val toReturn = mutableListOf<AudioTrack>()
            toReturn.add(player.playingTrack)

            if (skipAmount > 1) {
                val tracksToRemove = mutableListOf<AudioTrack>()

                val amountFromStack = skipAmount - 1
                val amountFromQueue =
                    if (interruptedStack.size < amountFromStack) {
                        amountFromStack - interruptedStack.size
                    } else {
                        0
                    }

                val stackRemove = interruptedStack.take(amountFromStack)
                interruptedStack.removeAll(stackRemove)
                tracksToRemove.addAll(stackRemove)

                val queueRemove = trackQueue.take(amountFromQueue)
                tracksToRemove.addAll(queueRemove)
                trackQueue.removeAll(queueRemove)

                toReturn.addAll(tracksToRemove)
            }

            if (requeue) {
                trackQueue.addAll(toReturn.map { it.makeClone() })
            }

            startNextTrack()

            // Return the skipped Tracks as List of TrackInfo
            toReturn.map { it.makeInfo() }
        }
    }

    override fun windCurrentTrack(deltaMs: Long): TrackInfo? =
        synchronized(playerAndQueueLock) {
            player.playingTrack?.run {
                position = (position + deltaMs).coerceIn(0L, duration)

                // return new TrackInformation
                makeInfo()
            }
        }

    override fun deleteFromQueue(index: Int): TrackInfo? =
        synchronized(playerAndQueueLock) {
            if (index >= 0 && index < trackQueue.size)
                trackQueue.removeAt(index).makeInfo()
            else null
        }

    override fun clearQueue() {
        synchronized(playerAndQueueLock) {
            trackQueue.clear()
        }
    }

    override fun shuffleQueue() {
        synchronized(playerAndQueueLock) {
            trackQueue.shuffle()
        }
    }

    override fun getCurrentTrackInfo(): TrackInfo? =
        synchronized(playerAndQueueLock) {
            // Return TrackInfo of the playing track else null (Elvis-Op is omitted)
            player.playingTrack?.makeInfo()
        }

    override fun getQueueInfo(): List<TrackInfo> =
        synchronized(playerAndQueueLock) {
            trackQueue.map { it.makeInfo() }
        }

    override fun togglePause(changeValue: Boolean): Boolean =
        synchronized(playerAndQueueLock) {
            if (changeValue) {
                player.isPaused = !player.isPaused
            }
            player.isPaused
        }

    override fun toggleRepeat(changeValue: Boolean): Boolean =
        synchronized(playerAndQueueLock) {
            if (changeValue) {
                isRepeat.set(!isRepeat.get())
            }
            isRepeat.get()
        }

    override fun togglePermanentShuffle(changeValue: Boolean): Boolean =
        synchronized(playerAndQueueLock) {
            if (changeValue) {
                isShuffle.set(!isShuffle.get())
            }
            isShuffle.get()
        }

    override fun onPlayerPause(player: AudioPlayer?) {
        synchronized(playerAndQueueLock) {
            logger().info("Player for $this was paused")
        }
    }

    override fun onPlayerResume(player: AudioPlayer?) {
        synchronized(playerAndQueueLock) {
            logger().info("Player for $this was resumed")
        }
    }

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {
        synchronized(playerAndQueueLock) {
            logger().info("$this started playing track ${track?.makeInfo()}}")
        }
    }

    override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason?) {
        synchronized(playerAndQueueLock) {
            logger().info("$this finished playing track ${track?.makeInfo()}} for reason $endReason")

            if (endReason?.mayStartNext == true) {
                logger().info("$this is starting next audio track")
                startNextTrack()
            }

            if (isRepeat.get()) {
                track?.let { trackQueue.add(track.makeClone()) }
            }
        }
    }

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack?, exception: FriendlyException?) {
        // Logging needs no synchronization
        logger().error("$this encountered an error when playing ${track?.makeInfo()}. ", exception)
        logger().error("Signaling the end for the track")

        // Already thread-safe
        onTrackEnd(player, track, AudioTrackEndReason.LOAD_FAILED)
    }

    override fun onTrackStuck(player: AudioPlayer?, track: AudioTrack?, thresholdMs: Long) {
        // Logging needs no synchronization
        logger().warn("$this encountered an stuck track: ${track?.makeInfo()}. Help me step-track :blush: ;-).")
        logger().warn("$this is helping the track by skipping it.")

        // Already thread-safe
        onTrackEnd(player, track, AudioTrackEndReason.LOAD_FAILED)
    }

    override fun onTrackStuck(player: AudioPlayer?, track: AudioTrack?, thresholdMs: Long, stackTrace: Array<out StackTraceElement>?) {
        // Logging needs no synchronization
        logger().warn("$this encountered an stuck track ${track?.makeInfo()} with StackTrace...")
        stackTrace?.forEach {
            logger().warn("    $it")
        }

        // Already thread-safe
        onTrackStuck(player, track, thresholdMs)
    }

    override fun toString(): String {
        return "JdaGuildAudioController(guildId=$guildId)"
    }

    private fun createSendingHandler(player: AudioPlayer): AudioSendHandler {
        logger().info("Creating new SendingHandler for $this")
        return sendHandlerFactory.createAudioPlayerSendHandler(player)
    }

    /**
     * Startet den nächsten Track, abhängig von der folgenden Reihenfolge:
     *  - Stack der unterbrochenen Tracks
     *  - Die aktuelle Queue
     *  - *Kein Song mehr zum Abspielen verfügbar*
     */
    private fun startNextTrack() {
        synchronized(playerAndQueueLock) {
            val nextTrack =
                if (interruptedStack.isEmpty())
                    trackQueue.nextTrack(isShuffle.get())
                else
                    interruptedStack.removeFirst()

            player.startTrack(nextTrack, false)
        }
    }

    private fun addTrack(track: AudioTrack, playInstant: Boolean, interruptCurrent: Boolean) {
        synchronized(playerAndQueueLock) {
            if (interruptCurrent) {
                val interruptedTrack = player.playingTrack
                player.startTrack(track, false)
                interruptedTrack?.let {
                    interruptedStack.addFirst(it)
                    logger().info("Interrupted ${it.makeInfo()} to play ${track.makeInfo()}")
                }
                return
            } else {
                if (player.startTrack(track, !playInstant)) {
                    logger().info("Started ${track.makeInfo()} and ignored the Queue")
                    if (playInstant) {
                        logger().warn("Cleared interrupted songs, since a track was started immediately")
                        interruptedStack.clear()
                    }
                    return
                } else {
                    logger().info("Could not start ${track.makeInfo()}. Adding it to the queue instead")
                    trackQueue.add(track)
                }
            }

        }
    }

    /**
     * Durchsucht Youtube und gibt die Suchergebnisse der ersten Seite zurück (ca. 25, die genaue Anzahl kann variieren).
     */
    private fun searchYoutube(query: String): List<AudioTrack> {
        val ytSourceManager = playerManager.source(YoutubeAudioSourceManager::class.java) ?: null
        return if (ytSourceManager == null) {
            logger().warn("Kein YoutubeAudioSourceManager registriert.")
            emptyList()
        } else {
            val searchProvider = YoutubeSearchProvider()
            val toReturn: List<AudioTrack> = (searchProvider
                .loadSearchResult(query) { trackInfo ->
                    YoutubeAudioTrack(trackInfo, ytSourceManager)
                } as BasicAudioPlaylist)
                .tracks

            toReturn
        }
    }

    /**
     * Erstellt ein [TrackInfo]-Objekt für den [AudioTrack].
     * Durch diese Methode ist eine bessere Nutzung des Null-Check, bzw. Elvis-Operators möglich, als es mit einem Konstruktor der Fall wäre.
     */
    private fun AudioTrack.makeInfo(): TrackInfo = TrackInfo(this)
}
