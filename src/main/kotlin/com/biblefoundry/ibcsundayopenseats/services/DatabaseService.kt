package com.biblefoundry.ibcsundayopenseats.services

import org.apache.logging.log4j.kotlin.Logging
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object Visits : IntIdTable() {
    val date = varchar("date", 32)
    val email = varchar("email", 254)
    val partySize = integer("party_size")
    val attending = bool("attending")
}

object DatabaseService : Logging {
    fun connect(dbPath: String) {
        // connect to database
        val fullDbPath = File(dbPath).absolutePath
        Database.connect("jdbc:sqlite:$fullDbPath", "org.sqlite.JDBC")

        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(Visits)
        }
    }

    fun listVisits(date: String?, email: String?, attending: Boolean?) {
        transaction {
            val visits = Visits.selectAll()
            date?.let {
                visits.andWhere { Visits.date eq it }
            }
            email?.let {
                visits.andWhere { Visits.email eq it }
            }
            attending?.let {
                visits.andWhere { Visits.attending eq it }
            }

            var total = 0
            visits.forEach {
                logger.warn("${it[Visits.id]}: ${it[Visits.date]} - ${it[Visits.email]} - ${it[Visits.partySize]} - ${it[Visits.attending]}")
                total++
            }

            logger.warn("$total matching visits found")
        }
    }

    fun lookupVisit(date: String, email: String): ResultRow {
        return transaction {
            Visits.select { Visits.email eq email and (Visits.date eq date) }.single()
        }
    }

    fun lookupVisitsByEmail(email: String): List<ResultRow> {
        return transaction {
            Visits.select { Visits.email eq email }.toList()
        }
    }

    fun lookupVisitsByDate(date: String): List<ResultRow> {
        return transaction {
            Visits.select { Visits.date eq date }.toList()
        }
    }

    fun upsertVisit(date: String, email: String, partySize: Int, attending: Boolean) {
        return transaction {
            if (Visits.select { Visits.email eq email and (Visits.date eq date) }.count() == 0L) {
                // doesn't exist, so insert for the first time
                Visits.insert {
                    it[Visits.date] = date
                    it[Visits.email] = email
                    it[Visits.partySize] = partySize
                    it[Visits.attending] = attending
                }
            } else {
                // already exists, so update existing
                Visits.update ({ Visits.email eq email and (Visits.date eq date) }) {
                    it[Visits.partySize] = partySize
                    it[Visits.attending] = attending
                }
            }
        }
    }

    fun deleteVisit(date: String, email: String) {
        transaction {
            Visits.deleteWhere {
                Visits.date eq date and (Visits.email eq email)
            }
        }
    }

    fun deleteAllVisits() {
        transaction {
            Visits.deleteAll()
        }
    }
}