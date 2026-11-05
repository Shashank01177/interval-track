package com.intervaltrack.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intervaltrack.app.data.model.AppSummary
import com.intervaltrack.app.ui.theme.AccentIndigo
import com.intervaltrack.app.ui.theme.BorderDark
import com.intervaltrack.app.ui.theme.CardDark
import com.intervaltrack.app.ui.theme.TextPrimary
import com.intervaltrack.app.ui.theme.TextSecondary

@Composable
fun AppFilterRow(
    summaries: List<AppSummary>,
    selectedPackage: String?,
    totalSessionsCount: Int,
    onSelectPackage: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All Apps" Chip
        val isAllSelected = selectedPackage == null
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isAllSelected) AccentIndigo else CardDark)
                .border(1.dp, if (isAllSelected) AccentIndigo else BorderDark, RoundedCornerShape(20.dp))
                .clickable { onSelectPackage(null) }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "All Apps",
                    color = if (isAllSelected) Color.White else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isAllSelected) FontWeight.SemiBold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isAllSelected) Color.White.copy(alpha = 0.25f) else Color(0xFF0F172A))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$totalSessionsCount",
                        color = if (isAllSelected) Color.White else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Individual app chips
        summaries.forEach { summary ->
            Spacer(modifier = Modifier.width(8.dp))
            val isSelected = selectedPackage == summary.packageName
            val dotColor = try {
                Color(android.graphics.Color.parseColor(summary.colorHex))
            } catch (e: Exception) {
                AccentIndigo
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) AccentIndigo else CardDark)
                    .border(1.dp, if (isSelected) AccentIndigo else BorderDark, RoundedCornerShape(20.dp))
                    .clickable {
                        onSelectPackage(if (isSelected) null else summary.packageName)
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = summary.appName,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFF0F172A))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${summary.sessionCount}",
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
