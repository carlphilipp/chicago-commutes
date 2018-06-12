package fr.cph.chicago.util

import fr.cph.chicago.R
import fr.cph.chicago.core.App
import java.util.Calendar
import java.util.Date

object TimeUtil {
    /**
     * Get last update in minutes
     *
     * @return a string
     */
    fun lastUpdateInMinutes(): String {
        val lastUpdateInMinutes = StringBuilder()
        val currentDate = Calendar.getInstance().time
        val diff = getTimeDifference(App.instance.lastUpdate, currentDate)
        val hours = diff[0]
        val minutes = diff[1]
        if (hours == 0L && minutes == 0L) {
            lastUpdateInMinutes.append(App.instance.getString(R.string.time_now))
        } else {
            if (hours == 0L) {
                lastUpdateInMinutes.append(minutes).append(App.instance.getString(R.string.time_min))
            } else {
                lastUpdateInMinutes.append(hours).append(App.instance.getString(R.string.time_hour)).append(minutes).append(App.instance.getString(R.string.time_min))
            }
        }
        return lastUpdateInMinutes.toString()
    }

    /**
     * Get time difference between 2 dates
     *
     * @param date1 the date one
     * @param date2 the date two
     * @return a tab containing in 0 the hour and in 1 the minutes
     */
    private fun getTimeDifference(date1: Date, date2: Date): LongArray {
        val result = LongArray(2)
        val cal = Calendar.getInstance()
        cal.time = date1
        val t1 = cal.timeInMillis
        cal.time = date2
        var diff = Math.abs(cal.timeInMillis - t1)
        val day = 1000 * 60 * 60 * 24
        val hour = day / 24
        val minute = hour / 60
        diff %= day.toLong()
        val h = diff / hour
        diff %= hour.toLong()
        val m = diff / minute
        result[0] = h
        result[1] = m
        return result
    }
}
