package com.example.re_match.utils

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*


// formatting date
object DateUtils {
    fun formatRelativeTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        return when {
            DateUtils.isToday(timestamp) -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
            now - timestamp < DateUtils.WEEK_IN_MILLIS -> {
                DateUtils.getRelativeTimeSpanString(
                    timestamp, now, DateUtils.DAY_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_WEEKDAY
                ).toString()
            }
            else -> {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }
}