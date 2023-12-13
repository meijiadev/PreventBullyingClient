package com.mj.preventbullying.client

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.orhanobut.logger.Logger
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.net.URL

/**
 * Create by MJ on 2023/12/13.
 * Describe :
 */

object PcmPlayer {
    private var audioTrack: AudioTrack? = null
    private var bufferSize: Int = 0
    private var playing = false

    fun play(url: String) {
        kotlin.runCatching {
            val connection = URL(url).openConnection()
            val dataInputStream = DataInputStream(BufferedInputStream(connection.getInputStream()))
            val sampleRateInHz = 16000
            val channelConfig = AudioFormat.CHANNEL_OUT_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize =
                AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
            bufferSize = Math.max(minBufferSize, 1024)
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRateInHz,
                channelConfig,
                audioFormat,
                bufferSize,
                AudioTrack.MODE_STREAM
            )
            if (audioTrack != null) {
                audioTrack?.play()
                playing = true
                val buffer = ByteArray(bufferSize)
                while (playing && dataInputStream.available() > 0) {
                    val readSize = dataInputStream.read(buffer, 0, bufferSize)
                    if (readSize > 0) {
                        audioTrack?.write(buffer, 0, readSize)
                    }
                }
                dataInputStream.close()
                Logger.i("pcm音频播放结束")
            }
        }.onFailure {
            Logger.e("error:${it.message}")
        }
    }

    fun stop() {
        playing = false
        if (audioTrack != null) {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        }
    }
}