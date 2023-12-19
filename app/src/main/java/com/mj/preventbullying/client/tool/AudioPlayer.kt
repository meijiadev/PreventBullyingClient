package com.mj.preventbullying.client.tool

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import com.mj.preventbullying.client.MyApp
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
            mediaPlayer.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stop() {
        // if (mediaPlayer.isPlaying) {
        mediaPlayer.stop()
        //  }
    }

    fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }


    fun start() {
        if (!mediaPlayer.isPlaying)
            mediaPlayer.start()
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
        return mediaPlayer.isPlaying
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
    }

    interface AudioPlayerListener {
        fun onAudioPlayerStart(duration: Int)
        fun onAudioPlayerStop()
    }

    companion object {
        val instance: AudioPlayer by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AudioPlayer(MyApp.context)
        }
    }
}