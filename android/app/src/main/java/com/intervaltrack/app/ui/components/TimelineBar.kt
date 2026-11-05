package com.intervaltrack.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intervaltrack.app.data.model.AppSession
import com.intervaltrack.app.ui.theme.BorderDark
import com.intervaltrack.app.ui.theme.CardDark
import com.intervaltrack.app.ui.theme.TextMuted
import com.intervaltrack.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimelineBar(
    sessions: List<AppSession>,
    dayStartMs: Long,
    dayEndMs: Long,
    modifier: Modifier = Modifier,
    highlightPackage: String? = null,
    onHourSelected: ((Int) -> Unit)? = null
) {
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    val totalMs = (dayEndMs - dayStartMs).coerceAtLeast(1L)
    val now = System.currentTimeMillis()
    val isToday = now in dayStartMs..dayEndMs

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "24-Hour Visual Timeline",
                color = TextSecondary,
                fontSize = 13.sp
            )
            if (isToday) {
                Text(
                    text = "Live Day Progress",
                    color = Color(0xFF10B981),
                    fontSize = 11.sp
                )
            }
        }

        // Timeline Bar Canvas
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val fraction = offset.x / size.width
                        val hour = (fraction * 24).toInt().coerceIn(0, 23)
                        selectedHour = hour
                        onHourSelected?.invoke(hour)
                    }
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw hour division lines
                for (hour in 1..23) {
                    val x = (hour / 24f) * canvasWidth
                    drawLine(
                        color = Color(0xFF334155).copy(alpha = 0.4f),
                        start = Offset(x, 0f),
                        end = Offset(x, canvasHeight),
                        strokeWidth = 1f
                    )
                }

                // Draw session intervals
                sessions.forEach { session ->
                    val isHighlighted = highlightPackage == null || session.packageName == highlightPackage
                    if (isHighlighted) {
                        val sessionStart = session.startTime.coerceAtLeast(dayStartMs)
                        val sessionEnd = session.endTime.coerceAtMost(dayEndMs)

                        if (sessionEnd > sessionStart) {
                            val startFraction = (sessionStart - dayStartMs).toFloat() / totalMs
                            val endFraction = (sessionEnd - dayStartMs).toFloat() / totalMs

                            val left = startFraction * canvasWidth
                            val width = ((endFraction - startFraction) * canvasWidth).coerceAtLeast(3f)

                            val baseColor = try {
                                Color(android.graphics.Color.parseColor(session.colorHex))
                            } catch (e: Exception) {
                                Color(0xFF6366F1)
                            }

                            drawRoundRect(
                                color = baseColor.copy(alpha = if (session.isActiveNow) 1f else 0.85f),
                                topLeft = Offset(left, 4f),
                                size = Size(width, canvasHeight - 8f),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                    }
                }

                // If today, draw current time indicator
                if (isToday) {
                    val currentFraction = (now - dayStartMs).toFloat() / totalMs
                    if (currentFraction in 0f..1f) {
                        val currentX = currentFraction * canvasWidth
                        drawLine(
                            color = Color(0xFFF43F5E),
                            start = Offset(currentX, 0f),
                            end = Offset(currentX, canvasHeight),
                            strokeWidth = 3f
                        )
                        drawCircle(
                            color = Color(0xFFF43F5E),
                            radius = 4f,
                            center = Offset(currentX, 4f)
                        )
                    }
                }
            }
        }

        // Hour labels (00h, 06h, 12h, 18h, 24h)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("12 AM", "06 AM", "12 PM", "06 PM", "12 AM").forEach { label ->
                Text(
                    text = label,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
