package com.intervaltrack.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intervaltrack.app.ui.theme.AccentIndigo
import com.intervaltrack.app.ui.theme.BgDark
import com.intervaltrack.app.ui.theme.BorderDark
import com.intervaltrack.app.ui.theme.CardDark
import com.intervaltrack.app.ui.theme.TextMuted
import com.intervaltrack.app.ui.theme.TextPrimary
import com.intervaltrack.app.ui.theme.TextSecondary
import com.intervaltrack.app.ui.viewmodel.BatteryViewModel

@Composable
fun BatteryScreen(
    viewModel: BatteryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "chargingPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Battery Charge Alarm",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Full & custom percentage sound alert",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Master Enable Switch
            Switch(
                checked = uiState.isAlarmEnabled,
                onCheckedChange = { viewModel.toggleAlarmEnabled() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF10B981),
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = CardDark
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Battery Ring Gauge
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(CardDark)
                .border(2.dp, BorderDark, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Circular progress ring
            CircularProgressIndicator(
                progress = { (uiState.currentLevel / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.size(175.dp),
                color = when {
                    uiState.currentLevel >= uiState.targetPercentage -> Color(0xFF10B981)
                    uiState.currentLevel >= 80 -> Color(0xFF06B6D4)
                    uiState.currentLevel >= 20 -> AccentIndigo
                    else -> Color(0xFFEF4444)
                },
                strokeWidth = 12.dp,
                trackColor = Color(0xFF0F172A),
            )

            // Inner content
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (uiState.isCharging) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Charging",
                            tint = Color(0xFF10B981).copy(alpha = pulseAlpha),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "CHARGING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Text(
                    text = "${uiState.currentLevel}%",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Text(
                    text = "Target: ${uiState.targetPercentage}%",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Alarm Playing Warning Banner (if alarm is currently ringing)
        if (uiState.isAlarmPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF991B1B))
                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚡ TARGET REACHED: ${uiState.currentLevel}%!",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your phone has reached the set limit. Unplug charger.",
                        color = Color(0xFFFECACA),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.stopAlarm() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "STOP ALARM", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Target Percentage Selection Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CardDark)
                .border(1.dp, BorderDark, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alarm Target Level",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${uiState.targetPercentage}%",
                    color = AccentIndigo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = uiState.targetPercentage.toFloat(),
                onValueChange = { viewModel.setTargetPercentage(it.toInt()) },
                valueRange = 50f..100f,
                steps = 50,
                colors = SliderDefaults.colors(
                    thumbColor = AccentIndigo,
                    activeTrackColor = AccentIndigo,
                    inactiveTrackColor = Color(0xFF0F172A)
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Preset Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(80, 85, 90, 100).forEach { pct ->
                    val isSelected = uiState.targetPercentage == pct
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AccentIndigo else Color(0xFF0F172A))
                            .border(1.dp, if (isSelected) AccentIndigo else BorderDark, RoundedCornerShape(12.dp))
                            .clickable { viewModel.setTargetPercentage(pct) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$pct%",
                                color = if (isSelected) Color.White else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (pct == 100) "Full" else "Protect",
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sound & Vibration Settings Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CardDark)
                .border(1.dp, BorderDark, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Alert Preferences",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Alarm Sound Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = AccentIndigo)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Ringtone / Sound Alarm", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(text = "Plays loud alarm stream sound", color = TextMuted, fontSize = 11.sp)
                    }
                }
                Switch(
                    checked = uiState.soundEnabled,
                    onCheckedChange = { viewModel.toggleSound() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentIndigo
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vibration Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = Color(0xFF06B6D4))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Vibration Alert", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(text = "Repeating buzz pattern", color = TextMuted, fontSize = 11.sp)
                    }
                }
                Switch(
                    checked = uiState.vibrateEnabled,
                    onCheckedChange = { viewModel.toggleVibrate() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF06B6D4)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Test Alarm Sound Button
            Button(
                onClick = { viewModel.testAlarm() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isAlarmPlaying) Color(0xFFEF4444) else Color(0xFF0F172A),
                    contentColor = if (uiState.isAlarmPlaying) Color.White else AccentIndigo
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (uiState.isAlarmPlaying) Color(0xFFEF4444) else BorderDark)
            ) {
                Icon(
                    imageVector = if (uiState.isAlarmPlaying) Icons.Default.Stop else Icons.Default.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isAlarmPlaying) "Stop Test Alarm" else "Test Alarm Sound",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
