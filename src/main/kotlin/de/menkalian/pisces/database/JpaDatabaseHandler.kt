package de.menkalian.pisces.database

import de.menkalian.pisces.audio.data.TrackInfo
import de.menkalian.pisces.database.data.DatabaseSongEntry
import de.menkalian.pisces.database.data.PlaylistHandle
import de.menkalian.pisces.database.jpa.AliasDto
import de.menkalian.pisces.database.jpa.AliasRepository
import de.menkalian.pisces.database.jpa.PlaylistDto
import de.menkalian.pisces.database.jpa.PlaylistRepository
import de.menkalian.pisces.database.jpa.SettingsDto
import de.menkalian.pisces.database.jpa.SettingsRepository
import de.menkalian.pisces.database.jpa.SongEntryDto
import de.menkalian.pisces.database.jpa.SongEntryRepository
import de.menkalian.pisces.util.CommonHandlerImpl
import de.menkalian.pisces.variables.FlunderKey.Flunder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class JpaDatabaseHandler(
    val aliasRepo: AliasRepository,
    val settingsRepo: SettingsRepository,
    val playlistRepo: PlaylistRepository,
    val songEntryRepository: SongEntryRepository
) : IDatabaseHandler, CommonHandlerImpl() {

    override fun addCommandShortcut(guildId: Long, alias: String, original: String) {
        val origResolved = getFormalCommandName(guildId, original)
        aliasRepo.save(AliasDto(guildId = guildId, alias = alias, original = origResolved))
    }

    override fun getFormalCommandName(guildId: Long, alias: String): String {
        return aliasRepo.getFirstByGuildIdInAndAliasIsOrderByGuildIdDesc(listOf(0L, guildId), alias)?.original ?: alias
    }

    override fun getSettingsValue(guildId: Long, variable: String, default: String): String {
        return settingsRepo
            .getFirstByGuildIdInAndKeyIsOrderByGuildIdDesc(listOf(guildId, 0L), variable)
            ?.value ?: default
    }

    override fun setSettingsValue(guildId: Long, variable: String, value: String) {
        settingsRepo.save(SettingsDto(guildId = guildId, key = variable, value = value))
    }

    override fun createSavedSongEntryIfNotExists(audioTrackInfo: TrackInfo): Long {
        val existingEntry = songEntryRepository.findByUrl(audioTrackInfo.sourceUri)
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
        songEntryRepository.deleteAll(
            songEntryRepository.findAll()
                .filter { it.playlists.isEmpty() }
        )
    }

    override fun getOrCreatePlaylist(guildId: Long, name: String): PlaylistHandle {
        val playlist = playlistRepo.findByGuildIdAndName(guildId, name) ?: playlistRepo
            .save(
                PlaylistDto(
                    guildId = guildId,
                    name = name
                )
            )
        return PlaylistHandle(this, playlist.name, playlist.guildId)
    }

    override fun addToPlaylist(handle: PlaylistHandle, audioTrackInfo: TrackInfo): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeFromPlaylist(handle: PlaylistHandle, audioTrackInfo: TrackInfo) {
        val playlist = findPlaylistByHandle(handle)
        playlist?.songs
            ?.removeIf { it.url == audioTrackInfo.sourceUri }
        playlist?.let { playlistRepo.save(it) }
    }

    override fun deletePlaylist(handle: PlaylistHandle) {
        findPlaylistByHandle(handle)?.let {
            playlistRepo.delete(it)
        }
    }

    private fun findPlaylistByHandle(handle: PlaylistHandle): PlaylistDto? =
        playlistRepo.findByGuildIdAndName(handle.guildId, handle.name)

    override fun initialize() {
        // Clear generated
        aliasRepo.deleteAllByGuildId(0L)
        settingsRepo.deleteAllByGuildId(0L)

        // Default aliases are set externally

        // Default settings are set here
        setSettingsValue(0L, Flunder.Guild.Settings.Repeat.toString(), "false")
        setSettingsValue(0L, Flunder.Guild.Settings.Shuffle.toString(), "false")

        finishInitialization()
    }

    override fun deinitialize() {
        startDeinitialization()

        // cleanup song entries
        clearAllUnreferencedSongEntries()
    }
}