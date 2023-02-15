package de.menkalian.pisces.web

import de.menkalian.pisces.audio.IAudioHandler
import de.menkalian.pisces.audio.data.EPlayTrackResult
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.discord.IDiscordHandler
import de.menkalian.pisces.variables.FlunderKey.Flunder
import de.menkalian.pisces.web.data.NoMorePreloadsAvailableException
import de.menkalian.pisces.web.data.PreloadedTrackData
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class WebPreloadAudioController(
    private val audioHandler: IAudioHandler,
    private val discordHandler: IDiscordHandler,
    private val databaseHandler: IDatabaseHandler
) {
    private val MAX_TRACKS_PER_USER = 10
    private val activePreloads = mutableMapOf<Long, MutableMap<String, PreloadedTrackData>>()

    init {
        databaseHandler.addInitializationHandler {
            audioHandler.addInitializationHandler {
                preloadUser(0)
            }
        }
    }

    private fun preloadUser(userId: Long) {
        val preloadCount = databaseHandler.getSettingsValue(userId, Flunder.User.Preload.Track.n, "0").toIntOrNull() ?: 0
        val preloadMap = mutableMapOf<String, PreloadedTrackData>()
        for (i in 1..preloadCount) {
            val uuid = databaseHandler.getSettingsValue(userId, Flunder.User.Preload.Track.XXX.UUID.replace("XXX", "%03d".format(i)), "")
            val name = databaseHandler.getSettingsValue(userId, Flunder.User.Preload.Track.XXX.Name.replace("XXX", "%03d".format(i)), "Track $i")
            val url = databaseHandler.getSettingsValue(userId, Flunder.User.Preload.Track.XXX.Url.replace("XXX", "%03d".format(i)), "")

            val preloadData: PreloadedTrackData
            if (uuid.isNotBlank() && url.isNotBlank()) {
                val result = audioHandler.preloadController.preload(url, uuid)
                preloadData = PreloadedTrackData(
                    uuid,
                    name,
                    url,
                    result.first.let { it == EPlayTrackResult.ERROR || it == EPlayTrackResult.NOT_FOUND }
                )
            } else {
                preloadData = PreloadedTrackData(
                    uuid,
                    name,
                    url,
                    true
                )
            }
            preloadMap[uuid] = preloadData
        }
        activePreloads[userId] = preloadMap
    }

    private fun updatePreloads(userId: Long, data: List<PreloadedTrackData>) {
        databaseHandler.setSettingsValue(userId, Flunder.User.Preload.Track.n, data.size.toString())
        for (i in data.indices) {
            databaseHandler.setSettingsValue(userId, Flunder.User.Preload.Track.XXX.UUID.replace("XXX", "%03d".format(i + 1)), data[i].preloadUuid)
            databaseHandler.setSettingsValue(userId, Flunder.User.Preload.Track.XXX.Name.replace("XXX", "%03d".format(i + 1)), data[i].displayName)
            databaseHandler.setSettingsValue(userId, Flunder.User.Preload.Track.XXX.Url.replace("XXX", "%03d".format(i + 1)), data[i].preloadUrl)
        }
    }

    @GetMapping("preload/global")
    fun getGlobalPreloadedTracks(): List<PreloadedTrackData> {
        return activePreloads[0]!!.values.toList()
    }

    @PostMapping("preload/global")
    fun preloadGlobalTrack(
        authenticationToken: OAuth2AuthenticationToken,
        @RequestBody inputData: PreloadedTrackData
    ): PreloadedTrackData {
        if (authenticationToken.name != discordHandler.getOwnerUser().id) {
            throw InsufficientAuthenticationException("Only the owner may preload global tracks")
        }

        synchronized(audioHandler.preloadController) {
            val uuid = audioHandler.preloadController.getUniqueUuid()
            val result = audioHandler.preloadController.preload(inputData.preloadUrl, uuid)
            val created = PreloadedTrackData(
                uuid,
                inputData.displayName,
                inputData.preloadUrl,
                result.first.let { it == EPlayTrackResult.ERROR || it == EPlayTrackResult.NOT_FOUND }
            )

            activePreloads[0]!![uuid] = created
            updatePreloads(0, activePreloads[0]!!.values.toList())
            return created
        }
    }

    @GetMapping("preload/global/{uuid}")
    fun playGlobalPreloadedTrack(
        authenticationToken: OAuth2AuthenticationToken,
        @PathVariable uuid: String,
        @RequestParam(value = "instant", required = false, defaultValue = "false") instant: Boolean,
        @RequestParam(value = "interject", required = false, defaultValue = "false") interject: Boolean,
    ): Boolean {
        val audioController = audioHandler.getUserMatchingAudioController(authenticationToken.name.toLong())
        return audioController?.playPreloadedTrack(uuid, instant, interject) ?: false
    }

    @DeleteMapping("preload/global/{uuid}")
    fun playGlobalPreloadedTrack(
        authenticationToken: OAuth2AuthenticationToken,
        @PathVariable uuid: String,
    ): Boolean {
        if (authenticationToken.name != discordHandler.getOwnerUser().id) {
            throw InsufficientAuthenticationException("Only the owner may unload global tracks")
        }

        val deleted = activePreloads[0]!!.remove(uuid) != null
        updatePreloads(0, activePreloads[0]!!.values.toList())
        return deleted
    }

    @GetMapping("preload/personal")
    fun getPreloadedTracks(
        authenticationToken: OAuth2AuthenticationToken,
    ): List<PreloadedTrackData> {
        val uid = authenticationToken.name.toLong()
        ensureUser(uid)
        return activePreloads[uid]!!.values.toList()
    }

    @PostMapping("preload/personal")
    fun preloadTrack(
        authenticationToken: OAuth2AuthenticationToken,
        @RequestBody inputData: PreloadedTrackData
    ): PreloadedTrackData {
        val uid = authenticationToken.name.toLong()
        ensureUser(uid)

        if (activePreloads[uid]!!.size >= 10) {
            throw NoMorePreloadsAvailableException()
        }

        synchronized(audioHandler.preloadController) {
            val uuid = audioHandler.preloadController.getUniqueUuid()
            val result = audioHandler.preloadController.preload(inputData.preloadUrl, uuid)
            val created = PreloadedTrackData(
                uuid,
                inputData.displayName,
                inputData.preloadUrl,
                result.first.let { it == EPlayTrackResult.ERROR || it == EPlayTrackResult.NOT_FOUND }
            )

            activePreloads[uid]!![uuid] = created
            updatePreloads(uid, activePreloads[uid]!!.values.toList())
            return created
        }
    }

    @GetMapping("preload/personal/{uuid}")
    fun playPreloadedTrack(
        authenticationToken: OAuth2AuthenticationToken,
        @PathVariable uuid: String,
        @RequestParam(value = "instant", required = false, defaultValue = "false") instant: Boolean,
        @RequestParam(value = "interject", required = false, defaultValue = "false") interject: Boolean,
    ): Boolean {
        val uid = authenticationToken.name.toLong()
        ensureUser(uid)
        val audioController = audioHandler.getUserMatchingAudioController(authenticationToken.name.toLong())
        return audioController?.playPreloadedTrack(uuid, instant, interject) ?: false
    }

    @DeleteMapping("preload/personal/{uuid}")
    fun deletePreloadedTrack(
        authenticationToken: OAuth2AuthenticationToken,
        @PathVariable uuid: String,
    ): Boolean {
        val uid = authenticationToken.name.toLong()
        ensureUser(uid)
        val deleted = activePreloads[uid]!!.remove(uuid) != null
        updatePreloads(uid, activePreloads[uid]!!.values.toList())
        return deleted
    }

    private fun ensureUser(id: Long) {
        if (activePreloads.containsKey(id).not()) {
            preloadUser(id)
        }
    }
}