package com.biblefoundry.ibcsundayopenseats

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.toml

object SystemSpec : ConfigSpec() {
    val global_store by optional("seats.db")
    val parties_store by optional("parties.csv")
    val secret_key by required<String>()
}

object ServerSpec : ConfigSpec() {
    val public_base_url by optional("localhost")
    val port by optional(8080)
}

object EmailSpec : ConfigSpec() {
    val aws_region by optional("us-west-2")
    val from_email by required<String>()
    val contact_name by required<String>()
    val contact_email by required<String>()
    val dry_run_email_parties by optional(false)
}

object SchedulerSpec : ConfigSpec() {
    val start_weekday_time by required<String>()
    val end_weekday_time by required<String>()
}

class ConfigException(message: String) : Exception(message)

class Config(configFile: String) {
    val config = Config {
        addSpec(SystemSpec)
        addSpec(ServerSpec)
        addSpec(EmailSpec)
        addSpec(SchedulerSpec)
    }
        .from.toml.file(configFile)
        .from.env()
        .from.systemProperties()
}