package com.desklauncher.feature.files.ui

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.desklauncher.core.storage.DeskStorage
import com.desklauncher.feature.files.domain.model.FileCategory
import com.desklauncher.feature.files.domain.model.FileItem
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

/**
 * 다른 앱으로 열기·보내기 인텐트 (FILES-03).
 * 앱 파일은 FileProvider URI, 꺼내 둔 파일은 원래 content:// URI. 받는 앱에는 읽기 권한만 임시로 준다.
 */
class FileIntents @Inject internal constructor(
    private val storage: DeskStorage,
) {
    fun open(file: FileItem): Intent? = uriFor(file)?.let { uri ->
        Intent.createChooser(
            Intent(Intent.ACTION_VIEW).setDataAndType(uri, file.mimeType).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
            file.name,
        )
    }

    fun share(file: FileItem): Intent? = uriFor(file)?.let { uri ->
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).setType(file.mimeType).putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
            file.name,
        )
    }

    private fun uriFor(file: FileItem): Uri? = when (file.category) {
        FileCategory.Bookmarks -> file.location.toUri()
        FileCategory.Scans, FileCategory.Doodles -> storage.resolve(file.location)?.takeIf { it.exists() }?.let(storage::shareUri)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface FileIntentsEntryPoint {
    fun fileIntents(): FileIntents
}
