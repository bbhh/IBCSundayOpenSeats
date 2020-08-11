package com.biblefoundry.ibcsundayopenseats.cli

import com.biblefoundry.ibcsundayopenseats.services.DatabaseService
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import org.apache.logging.log4j.kotlin.Logging

class ListCommand : CliktCommand(), Logging {
    private val date by option("-d", "--date", help="Filter by date (like 2020-07-19)")
    private val email by option("-e", "--email", help="Filter by email")
    private val attending by option("-a", "--attending", help="Filter by attending").choice("yes", "no", "all")

    override fun run() {
        val attendingChoice = when (attending) {
            "yes" -> true
            "no" -> false
            else -> null
        }

        DatabaseService.listVisits(date, email, attendingChoice)
    }
}
