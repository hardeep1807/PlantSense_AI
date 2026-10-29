package com.plantsense.ai.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.text.DateFormat
import java.util.Date

@Composable
fun localizedDateTime(
    timestamp: Long,
    dateStyle: Int = DateFormat.MEDIUM,
    timeStyle: Int? = DateFormat.SHORT
): String {
    val locale = LocalConfiguration.current.locales[0]
    return remember(timestamp, locale, dateStyle, timeStyle) {
        val formatter = if (timeStyle == null) {
            DateFormat.getDateInstance(dateStyle, locale)
        } else {
            DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale)
        }
        formatter.format(Date(timestamp))
    }
}
