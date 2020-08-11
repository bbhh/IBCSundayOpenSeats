package com.biblefoundry.ibcsundayopenseats.scheduling

import org.apache.logging.log4j.kotlin.Logging
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder.newJob
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory

object Scheduler : Logging {
    private var startCron = ""
    private var endCron = ""
    private var contactEmail = ""

    fun configure(startCron: String, endCron: String, contactEmail: String) {
        this.startCron = startCron
        this.endCron = endCron
        this.contactEmail = contactEmail
    }

    fun start() {
        logger.info("Starting scheduler...")
        val schedulerFactory = StdSchedulerFactory()
        val scheduler = schedulerFactory.scheduler
        scheduleTriggers(scheduler)
        scheduler.start()
    }

    private fun scheduleTriggers(scheduler: Scheduler) {
        val groupName = "groupMain"
        
        // schedule start job
        val triggerStart = TriggerBuilder.newTrigger()
            .withIdentity("triggerStart", groupName)
            .startNow()
            .withSchedule(CronScheduleBuilder.cronScheduleNonvalidatedExpression(startCron))
            .build()
        val jobStart = newJob(StartJob::class.java)
            .withIdentity("jobStart", groupName)
            .build()
        jobStart.jobDataMap["contactEmail"] = contactEmail
        scheduler.scheduleJob(jobStart, triggerStart)

        // schedule end job
        val triggerEnd = TriggerBuilder.newTrigger()
            .withIdentity("triggerEnd", groupName)
            .startNow()
            .withSchedule(CronScheduleBuilder.cronScheduleNonvalidatedExpression(endCron))
            .build()
        val jobEnd = newJob(EndJob::class.java)
            .withIdentity("jobEnd", groupName)
            .build()
        jobEnd.jobDataMap["contactEmail"] = contactEmail
        scheduler.scheduleJob(jobEnd, triggerEnd)
    }
}