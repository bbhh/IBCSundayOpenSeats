package com.biblefoundry.ibcsundayopenseats.services

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.*
import com.biblefoundry.ibcsundayopenseats.stores.Party
import com.biblefoundry.ibcsundayopenseats.stores.PartyStore
import com.biblefoundry.ibcsundayopenseats.utils.DateUtils
import org.apache.logging.log4j.kotlin.Logging
import org.http4k.template.TemplateRenderer
import org.http4k.template.ViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

data class Email(
    val name: String,
    val sundayDate: String,
    val responseUrl: String,
    val deadlineDate: String,
    val contactName: String,
    val contactEmail: String
) : ViewModel

object EmailService : Logging {
    private const val CHARSET = "UTF-8"

    private lateinit var renderer: TemplateRenderer
    private lateinit var sesClient: AmazonSimpleEmailService
    private lateinit var publicBaseUrl: String
    private lateinit var fromEmail: String
    private lateinit var contactName: String
    private lateinit var contactEmail: String
    private var dryRunEmailParties = false

    fun configure(
        renderer: TemplateRenderer,
        region: String,
        publicBaseUrl: String,
        fromEmail: String,
        contactName: String,
        contactEmail: String,
        dryRunEmailParties: Boolean
    ) {
        sesClient = AmazonSimpleEmailServiceClientBuilder.standard()
            .withRegion(region)
            .build()

        this.renderer = renderer
        this.publicBaseUrl = publicBaseUrl
        this.fromEmail = fromEmail
        this.contactName = contactName
        this.contactEmail = contactEmail
        this.dryRunEmailParties = dryRunEmailParties
    }

    private fun generateEmailMessageText(name: String, sundayDate: String, responseUrl: String, deadlineDate: String): String {
        return "Dear $name,\n\nYou've been invited to attend Immanuel Bible Church in person this Sunday, $sundayDate. Will you be attending?\n\n" +
                "Please RSVP at $responseUrl before the deadline of $deadlineDate.\n\n" +
                "(You can always change your response at any time before the deadline by going to the same RSVP link above.)\n\n" +
                "Please do not reply to this automated email. If you have any questions or issues, please contact $contactName at $contactEmail.\n\n" +
                "Thank you!\nImmanuel Bible Church"
    }

    private fun generateEmailMessageHtml(name: String, sundayDate: String, responseUrl: String, deadlineDate: String): String {
        val viewModel = Email(
            name,
            sundayDate,
            responseUrl,
            deadlineDate,
            contactName,
            contactEmail
        )
        return renderer(viewModel)
    }

    fun emailParties(parties: List<Party>, sundayDate: LocalDate, deadline: LocalDateTime) {
        val sundayDateFormattedLong = sundayDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
        val sundayDateFormattedShort = sundayDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        val subject = "You're invited to attend IBC in person this Sunday ($sundayDateFormattedShort)"

        // transform deadline date
        val deadlineFormatted = deadline.format(DateUtils.fullDateTimeFormatter)
        val deadlineDate = Date.from(deadline.atZone(ZoneId.systemDefault()).toInstant())

        val baseUrl = "$publicBaseUrl/?t="
        parties.forEach { party ->
            // create JWS and URL
            val jwsResponse = JwsService.createJws(
                sundayDate,
                party,
                deadlineDate
            )
            val responseUrl = "$baseUrl$jwsResponse"

            val name = PartyStore.getName(party.email, "friend")

            // generate messages
            val messageHtml =
                generateEmailMessageHtml(
                    name,
                    sundayDateFormattedLong,
                    responseUrl,
                    deadlineFormatted
                )
            val messageText =
                generateEmailMessageText(
                    name,
                    sundayDateFormattedLong,
                    responseUrl,
                    deadlineFormatted
                )

            logger.info("Response URL: $responseUrl")

            // send email with messages
            sendEmail(
                party.email,
                subject,
                messageHtml,
                messageText,
                dryRunEmailParties
            )

            // add candidate to DB
            DatabaseService.upsertVisit(sundayDate.toString(), party.email, 0, false) // use a party size of 0 for now, until a response comes in
        }
    }

    fun sendEmail(emailAddress: String, subject: String, messageHtml: String, messageText: String = "", dryRun: Boolean = false) {
        val request = SendEmailRequest()
            .withSource(fromEmail)
            .withDestination(
                Destination().withToAddresses(emailAddress)
            )
            .withMessage(
                Message()
                    .withBody(
                        Body()
                            .withHtml(
                                Content().withCharset(CHARSET).withData(messageHtml)
                            )
                            .withText(
                                Content().withCharset(CHARSET).withData(messageText)
                            )
                    )
                    .withSubject(
                        Content().withCharset(CHARSET).withData(subject)
                    )
            )

        // send email
        if (!dryRun) {
            sesClient.sendEmail(request)
            logger.info("Email sent to $emailAddress")
        } else {
            logger.info("Dry run: email not sent to $emailAddress")
        }
    }
}
