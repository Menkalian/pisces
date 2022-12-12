package de.menkalian.pisces

import de.menkalian.pisces.util.logger
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
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
 */
@SpringBootApplication
class PiscesApplication {
    /**
     * Configure CORS to allow all origins for requests
     */
    @Bean
    fun corsConfiguration() = object : WebMvcConfigurer {
        override fun addCorsMappings(registry: CorsRegistry) {
            super.addCorsMappings(registry)
            logger().info("Configuring CORS")
            registry
                .addMapping("/**")
                .allowedMethods("GET", "PUT", "POST", "DELETE")
                .allowedOrigins("*")
        }
    }

    /**
     * Automatische Initialisierung aller Komponenten.
     *
     * Wird automatisch von `SpringBoot` ausgeführt.
     *
     * @param handlers Alle [IHandler]-Komponenten, die aktiv sind.
     */
    @Bean
    fun initialize(handlers: List<IHandler>): CommandLineRunner = CommandLineRunner {
        logger().info("Initializing components...")
        logger().debug("Found ${handlers.size} components.")
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

        logger().debug("Reading manual CLI-input.")
        while (scanner.hasNextLine()) {
            val input = scanner.nextLine()
            logger().trace("Read manual input line.")

            when (input.trim()) {
                "q", "quit" -> {
                    logger().info("Deinitializing components...")
                    handlers.forEach(IHandler::deinitialize)
                    logger().info("Components were deinitialized. Shutting down.")
                    exitProcess(0)
                }
                else        -> logger().warn("Unknown CLI-input \"$input\".")
            }
        }
    }
}
