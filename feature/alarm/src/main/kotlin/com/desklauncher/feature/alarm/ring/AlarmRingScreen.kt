package com.desklauncher.feature.alarm.ring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.alarm.R
import com.desklauncher.feature.alarm.domain.model.Alarm
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** 큰 시각 + 라벨 + 큰 버튼 두 개(엄지 영역). 잠결에도 누르기 쉽게 버튼을 크게. */
@Composable
internal fun AlarmRingScreen(
    alarm: Alarm?,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                text = (alarm?.time ?: LocalTime.now()).format(DateTimeFormatter.ofPattern("HH:mm")),
                fontSize = 88.sp,
                fontWeight = FontWeight.Light,
            )
            Text(
                text = alarm?.label?.ifBlank { null } ?: stringResource(R.string.alarm_tool_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.weight(1f))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = onSnooze, modifier = Modifier.fillMaxWidth().height(64.dp)) {
                    Text(stringResource(R.string.alarm_ring_snooze, alarm?.snoozeMinutes ?: 5), style = MaterialTheme.typography.titleMedium)
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DeskTabColor.Indigo.container, contentColor = DeskTabColor.Indigo.onContainer),
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                ) {
                    Text(stringResource(R.string.alarm_ring_dismiss), style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@DeskPreviews
@Composable
private fun AlarmRingScreenPreview() {
    DeskTheme(darkTheme = true) {
        AlarmRingScreen(Alarm("1", LocalTime.of(6, 30), label = "출근", createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH), onDismiss = {}, onSnooze = {})
    }
}
