package de.menkalian.pisces

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.util.Scanner
import kotlin.system.exitProcess

/**
 * Startmethode der Applikation.
 * Hier wird lediglich [runApplication] aufgerufen, um `SpringBoot` zu starten.
 */
fun main(args: Array<String>) {
    runApplication<PiscesApplication>(*args)
}

/**
 * Startklasse der `SpringBoot`-Applikation.
 *
 * @property log Logger der Klasse
 */
@SpringBootApplication
class PiscesApplication {
    val log = LoggerFactory.getLogger(this::class.java)!!

    /**
     * Automatische Initialisierung aller Komponenten.
     *
     * Wird automatisch von `SpringBoot` ausgeführt.
     *
     * @param handlers Alle [IHandler]-Komponenten, die aktiv sind.
     */
    @Bean
    fun initialize(handlers: List<IHandler>): CommandLineRunner = CommandLineRunner {
        log.info("Initializing components...")
        log.debug("Found ${handlers.size} components.")
        handlers.forEach(IHandler::initialize)
    }

    /**
     * Startet die manuelle Befehlseingabe.
     * Dadurch kann das Program vom Serverbetreiber in einem gewissen Rahmen gesteuert werden (z.B. Sauberes Herunterfahren des Programms).
     *
     * Wird automatisch von `SpringBoot` ausgeführt.
     *
     * @param handlers Alle [IHandler]-Komponenten, die aktiv sind.
     */
    @Bean
    fun runCli(handlers: List<IHandler>): CommandLineRunner = CommandLineRunner {
        val scanner = Scanner(System.`in`)

        log.debug("Reading manual CLI-input.")
        while (scanner.hasNextLine()) {
            val input = scanner.nextLine()
            log.trace("Read manual input line.")

            when (input.trim()) {
                "q", "quit" -> {
                    log.info("Deinitializing components...")
                    handlers.forEach(IHandler::deinitialize)
                    log.info("Components were deinitialized. Shutting down.")
                    exitProcess(0)
                }
                else        -> log.warn("Unknown CLI-input \"$input\".")
            }
        }
    }
}
