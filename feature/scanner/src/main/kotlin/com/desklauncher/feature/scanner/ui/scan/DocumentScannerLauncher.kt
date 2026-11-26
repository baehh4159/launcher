package com.desklauncher.feature.scanner.ui.scan

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.desklauncher.feature.scanner.domain.model.ScanResult
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

/**
 * ML Kit Document Scanner 실행기 (DOC-01). 스캐너 UI는 Google Play 서비스가 띄우므로 앱에 CAMERA 권한이 필요 없다.
 * 반환한 함수를 부르면 스캐너가 열린다. 모듈 미설치·Play 서비스 문제 등은 [onUnavailable].
 */
@Composable
internal fun rememberDocumentScannerLauncher(
    onScanned: (ScanResult) -> Unit,
    onUnavailable: () -> Unit,
): () -> Unit {
    val activity = LocalActivity.current
    val currentOnScanned by rememberUpdatedState(onScanned)
    val currentOnUnavailable by rememberUpdatedState(onUnavailable)
    val scanner = remember {
        GmsDocumentScanning.getClient(
            GmsDocumentScannerOptions.Builder()
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .setGalleryImportAllowed(true)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF, GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                .build(),
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val scan = GmsDocumentScanningResult.fromActivityResultIntent(result.data) ?: return@rememberLauncherForActivityResult
        val pdf = scan.pdf ?: return@rememberLauncherForActivityResult
        currentOnScanned(
            ScanResult(
                pdfUri = pdf.uri.toString(),
                pageCount = pdf.pageCount,
                firstPageImageUri = scan.pages?.firstOrNull()?.imageUri?.toString(),
            ),
        )
    }
    return remember(activity, scanner, launcher) {
        {
            if (activity == null) {
                currentOnUnavailable()
            } else {
                scanner.getStartScanIntent(activity)
                    .addOnSuccessListener { sender -> launcher.launch(IntentSenderRequest.Builder(sender).build()) }
                    .addOnFailureListener { currentOnUnavailable() }
            }
        }
    }
}
