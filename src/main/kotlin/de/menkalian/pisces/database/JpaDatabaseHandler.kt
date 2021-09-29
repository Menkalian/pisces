package de.menkalian.pisces.database

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.audio.data.TrackInfo
import de.menkalian.pisces.database.data.DatabaseSongEntry
import de.menkalian.pisces.database.data.PlaylistHandle
import de.menkalian.pisces.database.jpa.AliasDto
import de.menkalian.pisces.database.jpa.AliasRepository
import de.menkalian.pisces.database.jpa.JoinSoundDto
import de.menkalian.pisces.database.jpa.JoinSoundRepository
import de.menkalian.pisces.database.jpa.PlaylistDto
import de.menkalian.pisces.database.jpa.PlaylistRepository
import de.menkalian.pisces.database.jpa.SettingsDto
import de.menkalian.pisces.database.jpa.SettingsRepository
import de.menkalian.pisces.database.jpa.SongEntryDto
import de.menkalian.pisces.database.jpa.SongEntryRepository
import de.menkalian.pisces.util.CommonHandlerImpl
import de.menkalian.pisces.util.logger
import de.menkalian.pisces.variables.FlunderKey.Flunder
import org.springframework.context.annotation.Conditional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Standardimplementierung von [IDatabaseHandler].
 * Implementiert die Datenbankanbindung durch Nutzung der Java Persistence API (JPA).
 * Die Klassen für den Access sind im Package [de.menkalian.pisces.database.jpa].
 */
@Service
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.database.Handler.JpaDatabaseHandler"])
class JpaDatabaseHandler(
    val aliasRepo: AliasRepository,
    val settingsRepo: SettingsRepository,
    val playlistRepo: PlaylistRepository,
    val joinSoundRepository: JoinSoundRepository,
    val songEntryRepository: SongEntryRepository
) : IDatabaseHandler, CommonHandlerImpl() {

    override fun addCommandShortcut(guildId: Long, alias: String, original: String) {
        val origResolved = getFormalCommandName(guildId, original)
        val existingEntry = aliasRepo.findByGuildIdAndAlias(guildId, alias)

        if (existingEntry != null) {
            logger().info("Updating existing alias for guild $guildId: \"$alias\"=\"$origResolved\"")
            existingEntry.original = origResolved
            aliasRepo.save(existingEntry)
        } else {
            logger().info("Creating command alias for guild $guildId: \"$alias\"=\"$origResolved\"")
            aliasRepo.save(AliasDto(guildId = guildId, alias = alias, original = origResolved))
        }
    }

    override fun getFormalCommandName(guildId: Long, alias: String): String {
        return aliasRepo.getFirstByGuildIdInAndAliasIsOrderByGuildIdDesc(listOf(0L, guildId), alias)?.original ?: alias
    }

    override fun getSettingsValue(guildId: Long, variable: String, default: String): String {
        return settingsRepo
            .getFirstByGuildIdInAndVariableNameIsOrderByGuildIdDesc(listOf(guildId, 0L), variable)
            ?.value ?: default
    }

    override fun setSettingsValue(guildId: Long, variable: String, value: String) {
        val existingSetting = settingsRepo.findByGuildIdAndVariableName(guildId, variable)

        if (existingSetting != null) {
            logger().info("Updating setting for guild $guildId: \"$variable\"=\"${existingSetting.value}\" -> \"$value\"")
            existingSetting.value = value
            settingsRepo.save(existingSetting)
        } else {
            logger().info("Saving setting for guild $guildId: \"$variable\"=\"$value\"")
            settingsRepo.save(SettingsDto(guildId = guildId, variableName = variable, value = value))
        }
    }

    override fun createSavedSongEntryIfNotExists(audioTrackInfo: TrackInfo): Long {
        val existingEntry = songEntryRepository.findByUrl(audioTrackInfo.sourceUri)

        if (existingEntry == null) {
            logger().info("Creating new song-entry for $audioTrackInfo")
        } else {
            logger().debug("Found existing song-entry for $audioTrackInfo")
        }

        return existingEntry?.id ?: songEntryRepository.save(
            SongEntryDto(
                url = audioTrackInfo.sourceUri,
                name = audioTrackInfo.title,
                duration = audioTrackInfo.length
            )
        ).id
    }

    override fun getSavedSongEntryInformation(id: Long): DatabaseSongEntry? {
        return songEntryRepository.findByIdOrNull(id)?.let { DatabaseSongEntry(it) }
    }

    override fun clearAllUnreferencedSongEntries() {
        logger().info("Clearing up unreferenced song-entries")
        songEntryRepository.deleteAll(
            songEntryRepository.findAll()
                .filter { it.playlists.isEmpty() && it.joinSounds.isEmpty() }
        )
    }

    override fun getOrCreatePlaylist(guildId: Long, name: String): PlaylistHandle {
        val existingPlaylist = playlistRepo.findByGuildIdAndName(guildId, name)

        if (existingPlaylist == null) {
            logger().info("Creating new playlist \"$name\" for guild $guildId")
        } else {
            logger().debug("Found existing playlist \"$name\" for guild $guildId")
        }

        val playlist = existingPlaylist ?: playlistRepo
            .save(
                PlaylistDto(
                    guildId = guildId,
                    name = name
                )
            )
        return PlaylistHandle(this, playlist.name, playlist.guildId)
    }

    override fun getPlaylistSongs(handle: PlaylistHandle): List<DatabaseSongEntry> {
        return findPlaylistByHandle(handle)
            ?.songs
            ?.map { DatabaseSongEntry(it) }
            ?: listOf()
    }

    @Transactional
    override fun addToPlaylist(handle: PlaylistHandle, audioTrackInfo: TrackInfo): Boolean {
        logger().info("Adding $audioTrackInfo to $handle")
        val songId = createSavedSongEntryIfNotExists(audioTrackInfo)
        val songEntry = songEntryRepository.findById(songId)
        val playlist = findPlaylistByHandle(handle)

        songEntry.ifPresent {
            if (playlist != null) {
                playlist.songs.add(it)
                playlistRepo.save(playlist)
            }
        }

        return songEntry.isPresent && playlist != null
    }

    override fun removeFromPlaylist(handle: PlaylistHandle, audioTrackInfo: TrackInfo) {
        logger().info("Trying to remove $audioTrackInfo from $handle")
        val playlist = findPlaylistByHandle(handle)
        playlist?.songs
            ?.removeIf { it.url == audioTrackInfo.sourceUri }
        playlist?.let { playlistRepo.save(it) }
    }

    override fun deletePlaylist(handle: PlaylistHandle) {
        logger().info("Deleting playlist $handle")
        findPlaylistByHandle(handle)?.let {
            playlistRepo.delete(it)
        }
    }

    @Transactional
    override fun setUserJoinsound(userId: Long, audioTrackInfo: TrackInfo) {
        val songEntry = songEntryRepository.findByIdOrNull(createSavedSongEntryIfNotExists(audioTrackInfo)) ?: return
        val dto = joinSoundRepository.findByIdOrNull(userId) ?: JoinSoundDto(userId, songEntry)
        dto.song = songEntry
        joinSoundRepository.save(dto)
    }

    override fun getUserJoinsound(userId: Long): DatabaseSongEntry? {
        return joinSoundRepository
            .findByIdOrNull(userId)
            ?.song
            ?.let { DatabaseSongEntry(it) }
    }

    override fun initialize() {
        // Default aliases are set externally
        // Default settings are set here
        logger().debug("Writing default settings to database")
        setSettingsValue(0L, Flunder.Guild.Settings.Repeat, "false")
        setSettingsValue(0L, Flunder.Guild.Settings.Shuffle, "false")
        setSettingsValue(0L, Flunder.Guild.Settings.Prefix, "_")

        finishInitialization()
    }

    override fun deinitialize() {
        startDeinitialization()

        // cleanup song entries
        clearAllUnreferencedSongEntries()
    }

    /**
     * Sucht das [PlaylistDto]-Objekt anhand des [PlaylistHandle] aus der Datenbank.
     */
    private fun findPlaylistByHandle(handle: PlaylistHandle): PlaylistDto? =
        playlistRepo.findByGuildIdAndName(handle.guildId, handle.name)
}