package com.example.speech

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

private const val TAG = "AudioRecorderManager"

data class RecordingResult(
    val filePath: String,
    val durationMs: Long
)

class AudioRecorderManager(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var currentFile: File? = null
    private var startTimeMs: Long = 0L

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playingFilePath = MutableStateFlow<String?>(null)
    val playingFilePath: StateFlow<String?> = _playingFilePath.asStateFlow()

    fun recordingsDir(): File {
        val dir = File(context.filesDir, "recordings")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun startRecording(prefix: String = "rec"): String? {
        stopPlayback()
        if (_isRecording.value) {
            stopRecordingSafely()
        }
        return try {
            val outFile = File(recordingsDir(), "${prefix}_${System.currentTimeMillis()}.m4a")
            currentFile = outFile
            val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(96_000)
                setAudioSamplingRate(16_000)
                setOutputFile(outFile.absolutePath)
                prepare()
                start()
            }
            recorder = mediaRecorder
            startTimeMs = System.currentTimeMillis()
            _isRecording.value = true
            outFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording", e)
            cleanupRecorder()
            null
        }
    }

    /**
     * Stops recording safely and preserves the file even if interrupted by MIUI/HyperOS lifecycle.
     */
    fun stopRecordingSafely(): RecordingResult? {
        if (!_isRecording.value && recorder == null) return null
        val duration = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(300L)
        val file = currentFile
        try {
            recorder?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "MediaRecorder stop warning (interrupted or short recording)", e)
        } finally {
            cleanupRecorder()
        }
        return if (file != null && file.exists() && file.length() > 0L) {
            RecordingResult(file.absolutePath, duration)
        } else {
            null
        }
    }

    private fun cleanupRecorder() {
        try {
            recorder?.release()
        } catch (_: Exception) {
        }
        recorder = null
        _isRecording.value = false
    }

    fun playRecording(filePath: String, speed: Float = 1.0f, onComplete: () -> Unit = {}) {
        stopPlayback()
        val file = File(filePath)
        if (!file.exists()) {
            onComplete()
            return
        }
        try {
            val mp = MediaPlayer()
            mp.setDataSource(filePath)
            mp.prepare()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mp.playbackParams = PlaybackParams().setSpeed(speed.coerceIn(0.5f, 2.0f))
            }
            mp.setOnCompletionListener {
                stopPlayback()
                onComplete()
            }
            mp.start()
            player = mp
            _isPlaying.value = true
            _playingFilePath.value = filePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play recording", e)
            stopPlayback()
            onComplete()
        }
    }

    fun stopPlayback() {
        try {
            player?.stop()
        } catch (_: Exception) {
        }
        try {
            player?.release()
        } catch (_: Exception) {
        }
        player = null
        _isPlaying.value = false
        _playingFilePath.value = null
    }

    fun release() {
        if (_isRecording.value) {
            stopRecordingSafely()
        }
        stopPlayback()
    }
}
