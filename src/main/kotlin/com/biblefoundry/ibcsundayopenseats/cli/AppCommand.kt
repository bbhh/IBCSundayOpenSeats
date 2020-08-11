package com.biblefoundry.ibcsundayopenseats.cli

import com.biblefoundry.ibcsundayopenseats.Config
import com.biblefoundry.ibcsundayopenseats.ConfigException
import com.biblefoundry.ibcsundayopenseats.SystemSpec
import com.biblefoundry.ibcsundayopenseats.services.DatabaseService
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import org.apache.logging.log4j.kotlin.Logging
import org.fusesource.jansi.AnsiConsole
import kotlin.system.exitProcess

class AppCommand : CliktCommand(), Logging {
    private val configFile: String by option("-c", "--config", help="Configuration file").default("config.toml")

    override fun run() {
        // enable Jansi ANSI support (for Windows)
        AnsiConsole.systemInstall()

        // load configuration
        val config = try {
            Config(configFile)
        } catch (e: ConfigException) {
            println(e)
            exitProcess(1)
        }

        // connect to database
        DatabaseService.connect(config.config[SystemSpec.global_store])

        logger.info("=== Seats CLI ===")
    }
}
