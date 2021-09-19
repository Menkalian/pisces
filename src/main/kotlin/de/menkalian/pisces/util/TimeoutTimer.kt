package de.menkalian.pisces.util

import java.util.Timer
import java.util.TimerTask

class TimeoutTimer(val timeoutDurationMilliseconds: Long, val timeoutAction: () -> Unit) {
    private val timer: Timer = Timer()
    private val timerTask: TimerTask = object: TimerTask() {
        override fun run() {
            timeoutAction()
        }
    }

    init {
        start()
    }

    fun start() {
        timer.schedule(timerTask, timeoutDurationMilliseconds)
    }

    fun reset() {
        stop()
        start()
    }

    fun stop() {
        timer.cancel()
    }
}