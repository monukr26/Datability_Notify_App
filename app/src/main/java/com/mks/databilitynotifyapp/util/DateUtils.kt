package com.mks.databilitynotifyapp.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private val displayFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

    fun formatTimestamp(timestamp: Long): String {
        return try {
            displayFormat.format(Date(timestamp))
        } catch (e: Exception) {
            timestamp.toString()
        }
    }
}

