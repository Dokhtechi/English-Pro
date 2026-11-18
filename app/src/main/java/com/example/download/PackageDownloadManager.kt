package com.example.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.local.ContentPackageEntity
import com.example.data.local.EnglishProDatabase
import com.example.speech.SpeechModelSpec
import kotlinx.coroutines.flow.Flow
import java.io.File

class PackageDownloadManager(private val context: Context) {
    private val workManager = WorkManager.getInstance(context)
    private val db = EnglishProDatabase.getInstance(context)

    fun startOrResumePackageInstall(pkg: ContentPackageEntity) {
        val workName = "pkg_${pkg.id}"
        // Curated offline starter packages can install even without network; remote large packs work when online
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(
                workDataOf(
                    DownloadWorker.KEY_ITEM_ID to pkg.id,
                    DownloadWorker.KEY_ITEM_TYPE to "PACKAGE",
                    DownloadWorker.KEY_URL to pkg.downloadUrl,
                    DownloadWorker.KEY_EXPECTED_SIZE to pkg.sizeBytes
                )
            )
            .addTag(workName)
            .build()
        workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, request)
    }

    fun pauseDownload(itemId: String) {
        workManager.cancelUniqueWork("pkg_$itemId")
        workManager.cancelUniqueWork("model_$itemId")
    }

    suspend fun deletePackage(pkg: ContentPackageEntity) {
        workManager.cancelUniqueWork("pkg_${pkg.id}")
        if (pkg.localPath.isNotBlank()) {
            File(pkg.localPath).deleteRecursively()
        }
        if (pkg.id != "pkg_core_vocab") {
            db.vocabDao().deleteByPackage(pkg.id)
        }
        db.packageDao().upsertPackage(
            pkg.copy(
                status = "NOT_INSTALLED",
                downloadedBytes = 0L,
                progressPercent = 0,
                localPath = "",
                checksumSha256 = ""
            )
        )
    }

    fun downloadVoskModel(spec: SpeechModelSpec) {
        val workName = "model_${spec.id}"
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    DownloadWorker.KEY_ITEM_ID to spec.id,
                    DownloadWorker.KEY_ITEM_TYPE to "VOSK_MODEL",
                    DownloadWorker.KEY_URL to spec.downloadUrl,
                    DownloadWorker.KEY_EXPECTED_SIZE to spec.sizeBytes
                )
            )
            .addTag(workName)
            .build()
        workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, request)
    }

    fun observeWorkInfo(tag: String): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosByTagFlow(tag)
    }
}
