package com.desklauncher.feature.files.ui.component

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.files.domain.model.FileCategory
import com.desklauncher.feature.files.domain.model.FileItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 파일 한 줄: 종류 아이콘 · 이름 · 날짜·크기. [trailing]에 메뉴 등을 붙인다. */
@Composable
internal fun FileRow(
    file: FileItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val date = file.modifiedAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy.M.d HH:mm", locale))
    val size = file.sizeBytes?.let { Formatter.formatShortFileSize(context, it) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = when {
                file.mimeType == "application/pdf" -> Icons.Outlined.PictureAsPdf
                file.mimeType.startsWith("image/") -> Icons.Outlined.Image
                else -> Icons.AutoMirrored.Outlined.InsertDriveFile
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(Modifier.weight(1f)) {
            Text(file.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.MiddleEllipsis)
            Text(listOfNotNull(date, size).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun FileRowPreview() {
    DeskTheme {
        FileRow(FileItem("1", "전세 계약서.pdf", FileCategory.Scans, "documents/1.pdf", "application/pdf", 1_240_000, Instant.parse("2026-11-26T06:00:00Z")), onClick = {})
    }
}
