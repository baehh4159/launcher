package com.desklauncher.core.storage

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱 내부 저장소(filesDir) 레이아웃 (PRD §8.5). DB에는 [relativePath]로 만든 상대 경로만 저장한다.
 * ```
 * documents/{id}.pdf
 * documents/thumbs/{id}.jpg
 * doodles/{id}.json, doodles/{id}.png
 * ```
 */
@Singleton
class DeskStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val root: File get() = context.filesDir

    fun documentPdf(id: String): File = File(dir(DocumentsDir), "$id.pdf")
    fun documentThumbnail(id: String): File = File(dir("$DocumentsDir/$ThumbsDir"), "$id.jpg")
    fun doodleStrokes(id: String): File = File(dir(DoodlesDir), "$id.json")
    fun doodleImage(id: String): File = File(dir(DoodlesDir), "$id.png")

    /** 상대 경로 → 파일 (경로 탈출 방지: filesDir 밖이면 null) */
    fun resolve(relativePath: String): File? {
        val file = File(root, relativePath).canonicalFile
        return file.takeIf { it.path.startsWith(root.canonicalPath + File.separator) }
    }

    fun relativePath(file: File): String = file.canonicalFile.relativeTo(root.canonicalFile).invariantSeparatorsPath

    /** 다른 앱에 넘길 content:// URI (공유 인텐트에 FLAG_GRANT_READ_URI_PERMISSION 필요) */
    fun shareUri(file: File): Uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)

    /** 지우지 못해도 예외를 던지지 않는다 (남은 파일은 다음 정리 때) */
    fun delete(relativePath: String?) {
        if (relativePath == null) return
        resolve(relativePath)?.let { runCatching { it.delete() } }
    }

    private fun dir(name: String): File = File(root, name).apply { mkdirs() }

    private companion object {
        const val DocumentsDir = "documents"
        const val ThumbsDir = "thumbs"
        const val DoodlesDir = "doodles"
    }
}
