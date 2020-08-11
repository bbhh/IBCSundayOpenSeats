package com.biblefoundry.ibcsundayopenseats.utils

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

data class WeekdayTime(val weekday: String, val hours: Int, val minutes: Int, val seconds: Int)

object DateUtils {
    val fullDateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy, h:mm a")

    private val weekdayTimeRegex = Regex("""([A-Z][a-z]{2}) (\d{2}):(\d{2}):(\d{2})""")

    private fun parseWeekdayTime(weekdayTimeStr: String): WeekdayTime? {
        val match = weekdayTimeRegex.find(weekdayTimeStr) ?: return null
        val (_, weekday, hours, minutes, seconds) = match.groupValues

        return WeekdayTime(weekday.toLowerCase(), hours.toInt(), minutes.toInt(), seconds.toInt())
    }

    private fun convertWeekday(weekdayStr: String): DayOfWeek? {
        return when (weekdayStr.toLowerCase()) {
            "sun" -> DayOfWeek.SUNDAY
            "mon" -> DayOfWeek.MONDAY
            "tue" -> DayOfWeek.TUESDAY
            "wed" -> DayOfWeek.WEDNESDAY
            "thu" -> DayOfWeek.THURSDAY
            "fri" -> DayOfWeek.FRIDAY
            "sat" -> DayOfWeek.SATURDAY
            else -> null
        }
    }

    fun convertWeekdayTimeToCron(weekdayTimeStr: String): String {
        val weekdayTime = parseWeekdayTime(weekdayTimeStr) ?: return ""
        return "${weekdayTime.seconds} ${weekdayTime.minutes} ${weekdayTime.hours} ? * ${weekdayTime.weekday}"
    }

    fun convertWeekdayTimeToLocalDateTime(weekdayTimeStr: String): LocalDateTime? {
        val weekdayTime = parseWeekdayTime(weekdayTimeStr) ?: return null
        val dayOfWeek = convertWeekday(weekdayTime.weekday)
        return LocalDateTime.now()
            .with(TemporalAdjusters.next(dayOfWeek))
            .withHour(weekdayTime.hours)
            .withMinute(weekdayTime.minutes)
            .withSecond(weekdayTime.seconds)
            .withNano(0)
    }
}