package com.biblefoundry.ibcsundayopenseats.cli

import com.biblefoundry.ibcsundayopenseats.services.DatabaseService
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import org.apache.logging.log4j.kotlin.Logging

class UpsertCommand : CliktCommand(), Logging {
    private val date by option("-d", "--date", help="Sunday date (like 2020-07-19)").required()
    private val email by option("-e", "--email", help="Primary email contact").required()
    private val partySize by option("-p", "--party-size", help="Party size").int().required()
    private val attending by option("-a", "--attending", help="Attending response").choice("yes", "no").required()

    override fun run() {
        val attendingChoice = attending == "yes"
        DatabaseService.upsertVisit(date, email, partySize, attendingChoice)
    }
}