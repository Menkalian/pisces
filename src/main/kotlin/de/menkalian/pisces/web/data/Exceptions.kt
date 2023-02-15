package de.menkalian.pisces.web.data

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND, reason = "No connected guild found")
class NoGuildConnectedException : RuntimeException()

@ResponseStatus(HttpStatus.BAD_REQUEST, reason = "No Track with searchterm found")
class NoTrackFoundException : RuntimeException()

@ResponseStatus(HttpStatus.BAD_REQUEST, reason = "No Track playing")
class NoTrackPlayingException : RuntimeException()

@ResponseStatus(HttpStatus.BAD_REQUEST, reason = "You have no preloads left")
class NoMorePreloadsAvailableException : RuntimeException()
