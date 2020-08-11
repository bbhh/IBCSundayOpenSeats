package com.biblefoundry.ibcsundayopenseats.stores

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.apache.logging.log4j.kotlin.Logging

data class Party(val name: String, val email: String)

object PartyStore : Logging {
    val parties = mutableMapOf<String, Party>()

    fun load(storePath: String) {
        csvReader().open(storePath) {
            readAllWithHeader().forEach { row ->
                val party = Party(
                    row["name"] ?: error("Missing name"),
                    row["email"] ?: error("Missing email")
                )
                parties[row.getValue("email")] = party
            }
        }
    }

    fun getName(email: String, defaultName: String): String {
        val party = parties.getOrElse(email) { return defaultName }
        return party.name
    }
}