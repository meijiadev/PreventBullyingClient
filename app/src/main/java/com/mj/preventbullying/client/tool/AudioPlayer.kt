package com.mj.preventbullying.client.tool

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import com.mj.preventbullying.client.app.MyApp
import com.orhanobut.logger.Logger
import java.io.IOException


/**
 * Create by MJ on 2023/12/18.
 * Describe :
 */

class AudioPlayer private constructor(context: Context) : MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener {
    private val listeners: MutableList<AudioPlayerListener> = ArrayList()
    private val mediaPlayer: MediaPlayer
    private val context: Context
    private val isPause = false

    init {
        this.context = context
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnCompletionListener(this)
    }

    fun addListener(listener: AudioPlayerListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: AudioPlayerListener) {
        listeners.remove(listener)
    }

    fun play(url: String?) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * assets文件夹下的MP3
     */
    fun playAssets(name: String) {
        kotlin.runCatching {
            Logger.i("播放的assets文件：$name")
            val afd = context.resources.assets.openFd(name)
            //   mediaPlayer.release()
            mediaPlayer.reset()
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
            mediaPlayer.prepareAsync()
        }.onFailure {
            Logger.e("error:$it")
        }
    }

    fun stop() {
        // if (mediaPlayer.isPlaying) {
        mediaPlayer.stop()
        //  }
    }

    fun pause() {
        kotlin.runCatching {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                for (listener in listeners) {
                    listener.onAudioPause()
                }
            }
        }.onFailure {
            Logger.i("error:$it")
        }
    }


    fun start() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            for (listener in listeners) {
                listener.onAudioRestart()
            }
        }

    }

    fun getDuration(): Int {
        return mediaPlayer.duration
    }

    fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
    }

    fun getPosition(): Int {
        return mediaPlayer.currentPosition
    }

    fun release() {
        mediaPlayer.release()
    }

    fun isPlaying(): Boolean {
        kotlin.runCatching {
            return mediaPlayer.isPlaying
        }.onFailure {
            Logger.i("error:$it")
            return false
        }
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        mp.start()
        Logger.i("音频时长：${mp.duration}")
        for (listener in listeners) {
            listener.onAudioPlayerStart(mp.duration)
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        for (listener in listeners) {
            listener.onAudioPlayerStop()
        }
        if (listeners.size == 0) {
            stop()
        }
    }

    interface AudioPlayerListener {
        fun onAudioPlayerStart(duration: Int)
        fun onAudioPlayerStop()

        fun onAudioPause()

        fun onAudioRestart()
    }

    companion object {
        val instance: AudioPlayer by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AudioPlayer(MyApp.context)
        }
    }
}