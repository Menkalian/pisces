package de.menkalian.pisces

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.util.Scanner
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    runApplication<PiscesApplication>(*args)
}

@SpringBootApplication
class PiscesApplication {
    val log = LoggerFactory.getLogger(this::class.java)!!

    @Bean
    fun initialize(handlers: List<IHandler>): CommandLineRunner = CommandLineRunner {
        log.info("Initializing components...")
        log.debug("Found ${handlers.size} components.")
        handlers.forEach(IHandler::initialize)
    }

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
