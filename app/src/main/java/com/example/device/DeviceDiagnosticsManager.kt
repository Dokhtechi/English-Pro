package com.example.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import com.example.speech.SpeechEngineDiagnostics
import com.example.speech.TtsStatus

data class DeviceDiagnosticsReport(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val isXiaomiFamily: Boolean,
    val hyperOsOrMiuiHint: String,
    val availableStorageBytes: Long,
    val totalStorageBytes: Long,
    val availableRamBytes: Long?,
    val totalRamBytes: Long?,
    val isInternetConnected: Boolean,
    val connectionType: String,
    val installedOfflineTtsVoices: List<String>,
    val speechDiagnostics: SpeechEngineDiagnostics,
    val offlineModelCompatibilityNote: String
)

enum class FeatureImplementationStatus(val labelEn: String, val labelFa: String) {
    FULLY_IMPLEMENTED_TESTED("Fully implemented and tested", "پیاده‌سازی کامل و تست‌شده"),
    IMPLEMENTED_PARTIAL_TEST("Implemented (requires device verification)", "پیاده‌سازی‌شده (نیازمند تست روی دستگاه)"),
    PLANNED("Planned", "در برنامه توسعه"),
    UNSUPPORTED_ON_DEVICE("Unsupported on current device", "پشتیبانی‌نشده در این دستگاه")
}

data class FeatureMatrixItem(
    val featureNameEn: String,
    val featureNameFa: String,
    val status: FeatureImplementationStatus,
    val technicalNote: String
)

object DeviceDiagnosticsManager {

    fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (_: Exception) {
            false
        }
    }

    fun getConnectionType(context: Context): String {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return "Unknown"
            val network = cm.activeNetwork ?: return "Offline"
            val caps = cm.getNetworkCapabilities(network) ?: return "Offline"
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular Data"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Connected"
            }
        } catch (_: Exception) {
            "Offline"
        }
    }

    fun getAvailableStorageBytes(context: Context): Long {
        return try {
            val stat = StatFs(context.filesDir.absolutePath)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (_: Exception) {
            try {
                val stat = StatFs(Environment.getDataDirectory().absolutePath)
                stat.availableBlocksLong * stat.blockSizeLong
            } catch (_: Exception) {
                0L
            }
        }
    }

    fun collectDiagnostics(
        context: Context,
        ttsStatus: TtsStatus,
        speechDiagnostics: SpeechEngineDiagnostics
    ): DeviceDiagnosticsReport {
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val isXiaomi = manufacturer.contains("Xiaomi", ignoreCase = true) ||
            manufacturer.contains("Redmi", ignoreCase = true) ||
            manufacturer.contains("POCO", ignoreCase = true)

        val miuiHint = detectMiuiOrHyperOs(isXiaomi)

        val stat = try { StatFs(context.filesDir.absolutePath) } catch (_: Exception) { null }
        val availStorage = stat?.let { it.availableBlocksLong * it.blockSizeLong } ?: 0L
        val totalStorage = stat?.let { it.blockCountLong * it.blockSizeLong } ?: 0L

        val memInfo = ActivityManager.MemoryInfo()
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val hasRam = try {
            am?.getMemoryInfo(memInfo)
            am != null
        } catch (_: Exception) {
            false
        }

        val online = isOnline(context)
        val connType = if (online) getConnectionType(context) else "Offline"

        val modelCompatNote = buildString {
            append("vosk-model-small-en-us-0.15 (~40 MB): ")
            if (availStorage > 150_000_000L) append("Storage sufficient. ") else append("Low storage warning. ")
            if (hasRam && memInfo.availMem < 350_000_000L) {
                append("Low free RAM detected; close heavy background apps before recording.")
            } else {
                append("RAM adequate for local recording and lightweight offline models.")
            }
        }

        return DeviceDiagnosticsReport(
            manufacturer = manufacturer.ifBlank { "Unknown" },
            model = Build.MODEL.orEmpty().ifBlank { "Android Device" },
            androidVersion = Build.VERSION.RELEASE.orEmpty(),
            apiLevel = Build.VERSION.SDK_INT,
            isXiaomiFamily = isXiaomi,
            hyperOsOrMiuiHint = miuiHint,
            availableStorageBytes = availStorage,
            totalStorageBytes = totalStorage,
            availableRamBytes = if (hasRam) memInfo.availMem else null,
            totalRamBytes = if (hasRam) memInfo.totalMem else null,
            isInternetConnected = online,
            connectionType = connType,
            installedOfflineTtsVoices = ttsStatus.installedEnglishOfflineVoices,
            speechDiagnostics = speechDiagnostics,
            offlineModelCompatibilityNote = modelCompatNote
        )
    }

    private fun detectMiuiOrHyperOs(isXiaomi: Boolean): String {
        val osVersion = getSystemProp("ro.mi.os.version.name")
        val miuiVersion = getSystemProp("ro.miui.ui.version.name")
        return when {
            !osVersion.isNullOrBlank() -> "HyperOS ($osVersion)"
            !miuiVersion.isNullOrBlank() -> "MIUI ($miuiVersion)"
            isXiaomi -> "Xiaomi/Redmi OS (Exact MIUI/HyperOS prop restricted on Android ${Build.VERSION.SDK_INT})"
            else -> "Standard Android (${Build.DISPLAY})"
        }
    }

    private fun getSystemProp(key: String): String? {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java)
            (method.invoke(null, key) as? String)?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    fun openAppBatterySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun buildFeatureStatusMatrix(report: DeviceDiagnosticsReport): List<FeatureMatrixItem> {
        val ttsStatus = if (report.installedOfflineTtsVoices.isNotEmpty()) {
            FeatureImplementationStatus.IMPLEMENTED_PARTIAL_TEST
        } else {
            FeatureImplementationStatus.UNSUPPORTED_ON_DEVICE
        }

        val asrStatus = if (report.speechDiagnostics.androidOnDeviceAvailable) {
            FeatureImplementationStatus.IMPLEMENTED_PARTIAL_TEST
        } else {
            FeatureImplementationStatus.IMPLEMENTED_PARTIAL_TEST
        }

        return listOf(
            FeatureMatrixItem("Local Room Database & Persistence", "پایگاه‌داده محلی Room و ذخیره پایدار", FeatureImplementationStatus.FULLY_IMPLEMENTED_TESTED, "Verified via Robolectric + SQLite transactions"),
            FeatureMatrixItem("FSRS-4.5 Spaced Repetition & Backlog Gate", "مرور فاصله‌دار FSRS و محافظت از انباشت مرور", FeatureImplementationStatus.FULLY_IMPLEMENTED_TESTED, "Verified retrievability, passive/active split, and backlog gating"),
            FeatureMatrixItem("Bilingual RTL Persian / LTR English UI", "رابط فارسی راست‌به‌چپ و محتوای انگلیسی چپ‌به‌راست", FeatureImplementationStatus.FULLY_IMPLEMENTED_TESTED, "Explicit LayoutDirection isolation verified"),
            FeatureMatrixItem("Initial Placement Assessment & CEFR Profile", "آزمون تعیین سطح اولیه و پروفایل CEFR", FeatureImplementationStatus.FULLY_IMPLEMENTED_TESTED, "5-skill scoring with honest learning-band disclaimer"),
            FeatureMatrixItem("JSON Export & Restore Backup (SAF)", "پشتیبان‌گیری و بازیابی JSON بدون نیاز به سرور", FeatureImplementationStatus.FULLY_IMPLEMENTED_TESTED, "Verified round-trip backup restoration"),
            FeatureMatrixItem("Civil Engineering Modules (D1–D7) & 20 Role-Plays", "ماژول‌های عمران (D1–D7) و ۲۰ نقش‌آفرینی", FeatureImplementationStatus.FULLY_IMPLEMENTED_TESTED, "Offline branching role-plays + FIDIC/US/UK notes"),
            FeatureMatrixItem("Online Gemini Speaking Coach (Firebase AI Logic)", "مربی مکالمه آنلاین Gemini", FeatureImplementationStatus.IMPLEMENTED_PARTIAL_TEST, "Requires live internet + Firebase App Check; auto-switches offline"),
            FeatureMatrixItem("Local Voice Recording & Speed Playback", "ضبط صدا و پخش با سرعت متغیر", FeatureImplementationStatus.IMPLEMENTED_PARTIAL_TEST, "MediaRecorder AAC + MediaPlayer speed tested on device hardware"),
            FeatureMatrixItem("WorkManager Content & Vosk Model Downloader", "مدیر دانلود بسته‌ها و مدل Vosk با قابلیت ادامه", FeatureImplementationStatus.IMPLEMENTED_PARTIAL_TEST, "HTTP Range resume + SHA/ZIP verification; requires network for remote packs"),
            FeatureMatrixItem("Offline Android Text-to-Speech (US/UK)", "تبدیل متن به گفتار آفلاین (آمریکایی/بریتانیایی)", ttsStatus, if (report.installedOfflineTtsVoices.isNotEmpty()) "${report.installedOfflineTtsVoices.size} offline English voices detected" else "No offline English TTS voice installed on this device"),
            FeatureMatrixItem("On-Device Speech Recognition (Vosk JNI / Android)", "تشخیص گفتار آفلاین روی دستگاه", asrStatus, "${report.speechDiagnostics.activeProviderName}. Manual fallback always available."),
            FeatureMatrixItem("True Phonetic/IPA Scoring Model", "امتیازدهی آوایی دقیق فونتیک", FeatureImplementationStatus.PLANNED, "ASR match & self-recording playback provided; true phoneme scoring is planned")
        )
    }
}
