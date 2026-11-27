package com.desklauncher.feature.files.domain.repository

import com.desklauncher.feature.files.domain.model.FileCategory
import com.desklauncher.feature.files.domain.model.FileItem
import kotlinx.coroutines.flow.Flow

interface FilesRepository {
    /** 최근 순 */
    fun files(category: FileCategory): Flow<List<FileItem>>
}
