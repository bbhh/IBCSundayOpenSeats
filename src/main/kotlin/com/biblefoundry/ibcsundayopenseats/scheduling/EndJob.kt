package com.biblefoundry.ibcsundayopenseats.scheduling

import com.biblefoundry.ibcsundayopenseats.services.DatabaseService
import com.biblefoundry.ibcsundayopenseats.services.EmailService
import com.biblefoundry.ibcsundayopenseats.services.Visits
import com.biblefoundry.ibcsundayopenseats.stores.PartyStore
import org.apache.logging.log4j.kotlin.Logging
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class EndJob : Job, Logging {
    override fun execute(context: JobExecutionContext) {
        logger.info("Starting final job...")

        // determine this Sunday
        val thisSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY))

        // seats are frozen at this point
        val attending = DatabaseService.lookupVisitsByDate(thisSunday.toString())
            .filter { it[Visits.attending] }
            .toList()
        val total = attending.map { it[Visits.partySize] }.sum()
        logger.info("attending ($total total from ${attending.size}): $attending")

        // email the Organizer with the list of accepted parties coming on Sunday
        val contactEmail = context.jobDetail.jobDataMap["contactEmail"] as String
        val subject = "Final list for this Sunday ($thisSunday)"
        val attendingList = attending.joinToString(separator = "") { "<li>${PartyStore.getName(it[Visits.email], "No name provided")} (${it[Visits.email]}): party of ${it[Visits.partySize]}</li>" }
        val messageHtml = """<p>For this Sunday ($thisSunday), $total have accepted their seats:</p>
            |<ol>
            |$attendingList
            |</ol>
        """.trimMargin()
        EmailService.sendEmail(contactEmail, subject, messageHtml)
    }
}