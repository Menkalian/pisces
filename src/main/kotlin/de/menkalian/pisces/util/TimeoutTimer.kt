package de.menkalian.pisces.util

import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicLong

/**
 * Hilfsklasse zur Implementierung eines Timeouts (z.B. um Ressourcen wieder freizugeben, nachdem mit einem Objekt für eine bestimmte Zeit nicht interagiert wurde).
 *
 * @property instanceNumber Einmalige Id dieses Timeouts zur Identifikation in Logs
 * @property timer Zugrundeliegende Timer-Instanz
 * @property currentTimerTask Aktion, die ausgeführt werden soll, wenn der Timeout abgelaufen ist.
 * @property timeoutDurationMilliseconds Timeout, nach dem [timeoutAction] ausgeführt wird.
 * @property timeoutAction Aktion die ausgeführt werden soll, wenn der Timeout abläuft
 *
 * @constructor Erstellt den Timer und started den Timeout **sofort**. Es ist kein weiterer Aufruf von [start] nötig.
 */
class TimeoutTimer(private val timeoutDurationMilliseconds: Long, private val timeoutAction: () -> Unit) {
    companion object {
        /**
         * Zählvariable, damit jeder Timer eine eigene Id erhalten kann.
         */
        private val instanceNumberCounter = AtomicLong(0L)
    }

    private val instanceNumber: Long = instanceNumberCounter.incrementAndGet()
    private val timer: Timer = Timer()
    private lateinit var currentTimerTask: TimerTask

    init {
        logger().debug("$this started with timeout $timeoutDurationMilliseconds ms")
        start()
    }

    /**
     * Startet diesen Timer.
     * Diese Methode muss von außen nur aufgerufen werden, wenn der Timeout nach einem Aufruf von [stop] neu gestartet werden soll.
     */
    fun start() {
        currentTimerTask = createTimerTask()
        timer.schedule(currentTimerTask, timeoutDurationMilliseconds)
    }

    /**
     * Setzt den Timeout zurück, damit der Countdown von neuem beginnt.
     */
    fun reset() {
        logger().trace("$this has been reset")
        currentTimerTask.cancel()
        timer.purge()
        start()
    }

    /**
     * Hält den Timer an bis erneut [start] oder [reset] aufgerufen werden.
     */
    fun stop() {
        timer.cancel()
    }

    override fun toString(): String {
        return "TimeoutTimer#$instanceNumber"
    }

    private fun createTimerTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                this@TimeoutTimer.logger().debug("Timeout ${this@TimeoutTimer} has run out. Calling timeout action")
                timeoutAction()
            }
        }
    }
}
