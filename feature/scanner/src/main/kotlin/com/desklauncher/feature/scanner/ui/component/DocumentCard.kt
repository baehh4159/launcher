package com.desklauncher.feature.scanner.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.scanner.R
import com.desklauncher.feature.scanner.domain.model.Document
import com.desklauncher.feature.scanner.ui.preview.CabinetPreviewData

/** 서류 카드: 첫 장 썸네일(A4 비율) + 제목 + 쪽수. */
@Composable
internal fun DocumentCard(
    document: Document,
    thumbnailLoader: ThumbnailLoader,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    menu: @Composable () -> Unit = {},
) {
    val thumbnail by produceState<ImageBitmap?>(null, document.thumbnailPath) {
        value = document.thumbnailPath?.let { thumbnailLoader.load(it) }
    }
    Card(modifier = modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(A4Ratio)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            val image = thumbnail
            if (image != null) {
                Image(bitmap = image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().aspectRatio(A4Ratio))
            } else {
                Icon(Icons.Outlined.Description, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
            }
            Box(Modifier.align(Alignment.TopEnd)) { menu() }
        }
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(document.title, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(stringResource(R.string.cabinet_pages, document.pageCount), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private const val A4Ratio = 210f / 297f

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun DocumentCardPreview() {
    DeskTheme {
        DocumentCard(CabinetPreviewData.documents.first(), thumbnailLoader = { null }, onClick = {}, modifier = Modifier.size(160.dp, 280.dp))
    }
}
