package com.intervaltrack.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intervaltrack.app.ui.theme.CardDark
import com.intervaltrack.app.ui.theme.TextPrimary
import com.intervaltrack.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DateSelector(
    selectedDate: Calendar,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val dateText = when {
        isToday -> "Today"
        isYesterday(selectedDate) -> "Yesterday"
        else -> SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(selectedDate.time)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(CardDark)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousDay) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous Day",
                tint = TextSecondary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = dateText,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        IconButton(
            onClick = onNextDay,
            enabled = !isToday
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next Day",
                tint = if (isToday) Color(0xFF334155) else TextSecondary
            )
        }
    }
}

private fun isYesterday(calendar: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
}
