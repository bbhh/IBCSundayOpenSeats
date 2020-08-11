package com.biblefoundry.ibcsundayopenseats.cli

import com.biblefoundry.ibcsundayopenseats.services.DatabaseService
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.apache.logging.log4j.kotlin.Logging

class DeleteCommand : CliktCommand(), Logging {
    private val date by option("-d", "--date", help="Sunday date (like 2020-07-19)").required()
    private val email by option("-e", "--email", help="Primary email contact").required()

    override fun run() {
        DatabaseService.deleteVisit(date, email)
    }
}
