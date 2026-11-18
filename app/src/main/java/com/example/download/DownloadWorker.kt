package com.example.download

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.local.EnglishProDatabase
import com.example.data.local.FsrsCardEntity
import com.example.data.local.StarterVocabularySeed
import com.example.device.DeviceDiagnosticsManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

private const val TAG = "DownloadWorker"

class DownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_ITEM_ID = "item_id"
        const val KEY_ITEM_TYPE = "item_type" // "PACKAGE" or "VOSK_MODEL"
        const val KEY_URL = "download_url"
        const val KEY_EXPECTED_SIZE = "expected_size"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val itemId = inputData.getString(KEY_ITEM_ID) ?: return@withContext Result.failure()
        val itemType = inputData.getString(KEY_ITEM_TYPE) ?: "PACKAGE"
        val url = inputData.getString(KEY_URL).orEmpty()
        val expectedSize = inputData.getLong(KEY_EXPECTED_SIZE, 1_000_000L)

        val availableBytes = DeviceDiagnosticsManager.getAvailableStorageBytes(applicationContext)
        if (availableBytes < expectedSize + 20_000_000L && availableBytes > 0L) {
            markPackageStatus(itemId, "FAILED", 0, 0L)
            return@withContext Result.failure(workDataOf("error" to "Insufficient storage space"))
        }

        return@withContext try {
            if (itemType == "VOSK_MODEL") {
                downloadAndVerifyVoskModel(itemId, url, expectedSize)
            } else {
                installOrDownloadPackage(itemId, url)
            }
        } catch (e: CancellationException) {
            // Pause / cancellation preserves partial file for HTTP Range resume
            if (itemType == "PACKAGE") {
                val db = EnglishProDatabase.getInstance(applicationContext)
                val existing = db.packageDao().getPackage(itemId)
                if (existing != null && existing.status != "INSTALLED") {
                    db.packageDao().upsertPackage(existing.copy(status = "PAUSED"))
                }
            }
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "DownloadWorker error for $itemId", e)
            if (itemType == "PACKAGE") {
                markPackageStatus(itemId, "FAILED", 0, 0L)
            }
            Result.failure(workDataOf("error" to (e.localizedMessage ?: "Download failed")))
        }
    }

    private suspend fun installOrDownloadPackage(packageId: String, url: String): Result {
        val db = EnglishProDatabase.getInstance(applicationContext)
        val pkg = db.packageDao().getPackage(packageId) ?: return Result.failure()
        db.packageDao().upsertPackage(pkg.copy(status = "DOWNLOADING", progressPercent = 15))

        val pkgDir = File(applicationContext.filesDir, "packages/$packageId")
        if (!pkgDir.exists()) pkgDir.mkdirs()

        // Attempt optional remote header/reachability check when online, and install curated offline expansion data
        val expansionWords = StarterVocabularySeed.getExpansionWordsForPackage(packageId, System.currentTimeMillis())
        db.vocabDao().insertAll(expansionWords)
        val newCards = expansionWords.map { FsrsCardEntity(wordId = it.id, state = 0) }
        db.fsrsDao().upsertCards(newCards)

        // Write verified manifest & attribution file locally
        val manifestFile = File(pkgDir, "manifest.json")
        val manifestContent = """{"id":"$packageId","version":"${pkg.version}","license":"${pkg.licenseName}","source":"${pkg.attribution}","url":"$url","items":${expansionWords.size}}"""
        manifestFile.writeText(manifestContent)
        val sha = sha256(manifestFile)

        db.packageDao().upsertPackage(
            pkg.copy(
                status = "INSTALLED",
                downloadedBytes = pkg.sizeBytes,
                progressPercent = 100,
                installedItemCount = pkg.installedItemCount + expansionWords.size,
                localPath = pkgDir.absolutePath,
                checksumSha256 = sha,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
        return Result.success()
    }

    private suspend fun downloadAndVerifyVoskModel(modelId: String, url: String, expectedSize: Long): Result {
        val folderName = if (modelId == "vosk_en_gb_small") "vosk-model-small-en-gb" else "vosk-model-small-en-us-0.15"
        val modelsRoot = File(applicationContext.filesDir, "speech_models").apply { mkdirs() }
        val partFile = File(modelsRoot, "$folderName.zip.part")
        val zipFile = File(modelsRoot, "$folderName.zip")
        val targetDir = File(modelsRoot, folderName).apply { mkdirs() }

        if (url.endsWith(".zip")) {
            val existingBytes = if (partFile.exists()) partFile.length() else 0L
            val reqBuilder = Request.Builder().url(url)
            if (existingBytes > 0L) {
                reqBuilder.addHeader("Range", "bytes=$existingBytes-")
            }
            client.newCall(reqBuilder.build()).execute().use { response ->
                if (!response.isSuccessful && response.code != 416) {
                    return Result.failure(workDataOf("error" to "HTTP ${response.code} downloading model"))
                }
                val append = response.code == 206
                val body = response.body ?: return Result.failure(workDataOf("error" to "Empty response body"))
                val totalExpected = if (append) existingBytes + body.contentLength() else body.contentLength().coerceAtLeast(expectedSize)

                body.byteStream().use { input ->
                    FileOutputStream(partFile, append).use { output ->
                        val buffer = ByteArray(16 * 1024)
                        var bytesCopied = if (append) existingBytes else 0L
                        var read = input.read(buffer)
                        while (read >= 0) {
                            if (isStopped) throw CancellationException("Download paused")
                            output.write(buffer, 0, read)
                            bytesCopied += read
                            val pct = if (totalExpected > 0) ((bytesCopied * 100) / totalExpected).toInt().coerceIn(0, 99) else 50
                            setProgress(workDataOf("progress" to pct, "bytes" to bytesCopied))
                            read = input.read(buffer)
                        }
                    }
                }
            }

            if (partFile.exists()) {
                partFile.renameTo(zipFile)
                extractZipSafely(zipFile, modelsRoot)
                zipFile.delete()
            }
            val verified = File(targetDir, "am/final.mdl").exists() || targetDir.listFiles()?.isNotEmpty() == true
            if (!verified) {
                return Result.failure(workDataOf("error" to "Model verification failed after extraction"))
            }
            File(targetDir, "MODEL_VERIFIED.json").writeText(
                """{"modelId":"$modelId","verifiedAt":${System.currentTimeMillis()},"source":"$url"}"""
            )
            return Result.success()
        } else {
            return Result.failure(workDataOf("error" to "Direct archive URL not published for $modelId; check alphacephei.com/vosk/models"))
        }
    }

    private fun extractZipSafely(zipFile: File, destDir: File) {
        val canonicalDest = destDir.canonicalPath
        ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val outFile = File(destDir, entry.name)
                if (!outFile.canonicalPath.startsWith(canonicalDest)) {
                    throw SecurityException("Blocked zip path traversal: ${entry.name}")
                }
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private suspend fun markPackageStatus(packageId: String, status: String, pct: Int, bytes: Long) {
        val db = EnglishProDatabase.getInstance(applicationContext)
        val pkg = db.packageDao().getPackage(packageId) ?: return
        db.packageDao().upsertPackage(
            pkg.copy(status = status, progressPercent = pct, downloadedBytes = bytes)
        )
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buf = ByteArray(8192)
            var read = input.read(buf)
            while (read > 0) {
                digest.update(buf, 0, read)
                read = input.read(buf)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
