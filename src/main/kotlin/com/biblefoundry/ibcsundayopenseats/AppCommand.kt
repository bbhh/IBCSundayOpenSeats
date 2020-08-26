package com.biblefoundry.ibcsundayopenseats

import com.biblefoundry.ibcsundayopenseats.scheduling.Scheduler
import com.biblefoundry.ibcsundayopenseats.services.DatabaseService
import com.biblefoundry.ibcsundayopenseats.services.EmailService
import com.biblefoundry.ibcsundayopenseats.services.JwsService
import com.biblefoundry.ibcsundayopenseats.services.ResponseService
import com.biblefoundry.ibcsundayopenseats.stores.PartyStore
import com.biblefoundry.ibcsundayopenseats.utils.DateUtils
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import org.apache.logging.log4j.kotlin.Logging
import org.fusesource.jansi.AnsiConsole
import org.http4k.core.*
import org.http4k.filter.CorsPolicy.Companion.UnsafeGlobalPermissive
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.http4k.template.HandlebarsTemplates
import kotlin.system.exitProcess

lateinit var config: Config

class AppCommand : CliktCommand(), Logging {
    private val configFile: String by option("-c", "--config", help="Configuration file").default("config.toml")

    override fun run() {
        // enable Jansi ANSI support (for Windows)
        AnsiConsole.systemInstall()

        // load configuration
        config = try {
            Config(configFile)
        } catch (e: ConfigException) {
            logger.error(e)
            exitProcess(1)
        }

        // connect to database
        DatabaseService.connect(config.config[SystemSpec.global_store])

        // load parties data
        PartyStore.load(config.config[SystemSpec.parties_store])

        // set up JWS service
        JwsService.configure(config.config[SystemSpec.secret_key])

        // set up Handlebars renderer
        val renderer = HandlebarsTemplates().HotReload("src/main/resources")
        
        val startWeekdayTime = config.config[SchedulerSpec.start_weekday_time]
        val startCron = DateUtils.convertWeekdayTimeToCron(startWeekdayTime)
        logger.info("start_weekday_time = $startWeekdayTime")
        logger.info("start_cron = $startCron")
        logger.info("start_localdate = ${DateUtils.convertWeekdayTimeToLocalDateTime(startWeekdayTime)}")
        val endWeekdayTime = config.config[SchedulerSpec.end_weekday_time]
        val endCron = DateUtils.convertWeekdayTimeToCron(endWeekdayTime)
        val endDate = DateUtils.convertWeekdayTimeToLocalDateTime(endWeekdayTime)
        if (endDate == null) {
            logger.error("scheduler.end_weekday_time is not a valid weekday-time")
            exitProcess(1)
        }
        logger.info("end_weekday_time = $endWeekdayTime")
        logger.info("end_cron = $endCron")
        logger.info("end_localdate = $endDate")

        // set up email service
        EmailService.configure(
            renderer,
            config.config[EmailSpec.aws_region],
            config.config[ServerSpec.public_base_url],
            config.config[EmailSpec.from_email],
            config.config[EmailSpec.contact_name],
            config.config[EmailSpec.contact_email],
            config.config[EmailSpec.dry_run_email_parties]
        )

        // start scheduler
        Scheduler.configure(
            startCron,
            endCron,
            endWeekdayTime,
            config.config[EmailSpec.contact_email]
        )
        Scheduler.start()

        // define web app
        val responseService = ResponseService(renderer)
        val app = ServerFilters.Cors(UnsafeGlobalPermissive)
            .then(routes(
                "/responses" bind responseService.routes,
                singlePageApp()
            ))

        // launch web server
        app.asServer(Jetty(config.config[ServerSpec.port])).start()
    }
}