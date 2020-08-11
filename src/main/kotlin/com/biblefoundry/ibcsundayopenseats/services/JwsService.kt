package com.biblefoundry.ibcsundayopenseats.services

import com.biblefoundry.ibcsundayopenseats.stores.Party
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.apache.logging.log4j.kotlin.Logging
import java.time.LocalDate
import java.util.*
import javax.crypto.SecretKey

object JwsService : Logging {
    lateinit var key: SecretKey

    fun configure(secretKey: String) {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))
    }

    fun createJws(
        sundayDate: LocalDate,
        party: Party,
        expirationDate: Date
    ): String? {
        return Jwts.builder()
            .claim("date", sundayDate.toString())
            .claim("email", party.email)
            .setExpiration(expirationDate)
            .signWith(key)
            .compact()
    }

    fun parseJws(jwsString: String): Jws<Claims>? {
        // TODO: handle JwsService - io.jsonwebtoken.ExpiredJwtException: JWT expired at 2020-06-14T06:38:36Z. Current time: 2020-06-14T06:48:55Z, a difference of 619332 milliseconds.  Allowed clock skew: 0 milliseconds.
        try {
            val jws = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwsString)
            return jws
        } catch (e: JwtException) {
            logger.error(e.toString())
        }

        return null
    }
}