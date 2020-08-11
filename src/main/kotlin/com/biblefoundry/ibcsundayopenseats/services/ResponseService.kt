package com.biblefoundry.ibcsundayopenseats.services

import com.biblefoundry.ibcsundayopenseats.stores.PartyStore
import com.biblefoundry.ibcsundayopenseats.utils.DateUtils
import org.apache.logging.log4j.kotlin.Logging
import org.http4k.core.*
import org.http4k.core.Response
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.template.TemplateRenderer
import org.http4k.template.ViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import org.http4k.format.Jackson.auto
import java.time.LocalDateTime
import java.time.ZoneId

data class Response(
    val textStyle: String,
    val name: String,
    val action: String,
    val partySize: Int,
    val pluralSuffix: String,
    val sundayDate: String,
    val greeting: String
) : ViewModel

data class SeatsResponse(
    val name: String,
    val email: String,
    val sundayDate: String,
    val partySize: Int,
    val attending: Boolean,
    val expirationDate: String
)

data class SeatsResponseUpdate(
    val partySize: Int,
    val attending: Boolean
)

class ResponseService(private val renderer: TemplateRenderer) : Logging {
    val seatsResponseLens = Body.auto<SeatsResponse>().toLens()
    val seatsResponseUpdateLens = Body.auto<SeatsResponseUpdate>().toLens()

    val routes = routes(
        "/{token}" bind Method.GET to { request -> getResponse(request) },
        "/{token}" bind Method.PUT to { request -> updateResponse(request) }
    )

    private fun getResponse(request: Request): Response {
        val token = request.path("token")!!

        // extract claims from JWS
        val claims = JwsService.parseJws(token)?.body ?: return Response(Status.FORBIDDEN).body("Sorry, this invitation has expired.")
        val email = claims["email"].toString()
        val name = PartyStore.getName(email, "friend")
        val sundayDate = LocalDate.parse(claims["date"].toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val sundayDateFormatted = sundayDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
        val expirationDate = LocalDateTime.ofInstant(claims.expiration.toInstant(), ZoneId.systemDefault())
        val expirationDateFormatted = expirationDate.format(DateUtils.fullDateTimeFormatter)

        // look up existing response in DB
        var attending = false
        var partySize = 1
        try {
            val visit = DatabaseService.lookupVisit(sundayDate.toString(), email)
            attending = visit[Visits.attending]
            partySize = visit[Visits.partySize]
        } catch (e: NoSuchElementException) {
            // update response in DB
            logger.info("No visit exists for $sundayDate - $email, so creating")
            DatabaseService.upsertVisit(sundayDate.toString(), email, partySize, attending)
        } catch (e: Exception) {
            // ignore exception
            logger.info(e)
        }

        val seatsResponse = SeatsResponse(name, email, sundayDateFormatted, partySize, attending, expirationDateFormatted)

        return seatsResponseLens(seatsResponse, Response(Status.OK))
    }

    private fun updateResponse(request: Request): Response {
        val token = request.path("token")!!

        // extract parameters from body
        val params = seatsResponseUpdateLens(request)

        // extract claims from JWS
        val claims = JwsService.parseJws(token)?.body ?: return Response(Status.FORBIDDEN).body("Invalid or expired token")
        val email = claims["email"].toString()
        val sundayDate = LocalDate.parse(claims["date"].toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        // update response in DB
        DatabaseService.upsertVisit(sundayDate.toString(), email, params.partySize, params.attending)

        return Response(Status.OK)
    }
}
