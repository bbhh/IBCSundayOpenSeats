package com.biblefoundry.ibcsundayopenseats.scheduling

import com.biblefoundry.ibcsundayopenseats.services.EmailService
import com.biblefoundry.ibcsundayopenseats.stores.PartyStore
import org.apache.logging.log4j.kotlin.Logging
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class StartJob : Job, Logging {
    override fun execute(context: JobExecutionContext) {
        logger.info("Starting start job...")

        logger.info(context.jobDetail.jobDataMap["contactEmail"] as String)

        // determine this Sunday
        val thisSunday = LocalDate.now().with(TemporalAdjusters.next(SUNDAY))
        logger.info("Today is ${LocalDate.now()}, this Sunday is $thisSunday")

        // email parties with a link to respond with
        val parties = PartyStore.parties.map { it.value }
        EmailService.emailParties(parties, thisSunday)

        // email the Organizer with the list of invited parties
        val contactEmail = context.jobDetail.jobDataMap["contactEmail"] as String
        val subject = "Invited list for this Sunday ($thisSunday)"
        val invitedList = parties.joinToString(separator = "") { "<li>${PartyStore.getName(it.email, "No name provided")} (${it.email})</li>" }
        val messageHtml = """<p>For this Sunday ($thisSunday), these ${parties.size} total parties were just invited now:</p>
            |<ol>
            |$invitedList
            |</ol>
        """.trimMargin()
        EmailService.sendEmail(contactEmail, subject, messageHtml)
    }
}