package com.mj.preventbullying.client.ui.dialog

import android.content.Context
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.lifecycleScope
import com.lxj.xpopup.core.CenterPopupView
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.tool.AudioPlayer
import com.mj.preventbullying.client.ui.adapter.PROCESSED_STATUS
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Create by MJ on 2023/12/27.
 * Describe :
 */

class AudioPlayDialog(context: Context) : CenterPopupView(context),
    AudioPlayer.AudioPlayerListener {
    private val seekBar: SeekBar? by lazy { findViewById(R.id.seekbar) }
    private val startTv: TextView? by lazy { findViewById(R.id.tv_start) }
    private val endTv: TextView? by lazy { findViewById(R.id.tv_end) }
    private val playIv: AppCompatImageView? by lazy { findViewById(R.id.play_iv) }
    private val closeIv: AppCompatImageView? by lazy { findViewById(R.id.close_iv) }

    private var duration: Int = 0
    private var isPlaying = false
    private var isSeekbarChaning = false
    private var audioUrl: String? = null

    //    private var
    override fun getImplLayoutId(): Int = R.layout.dialog_audio_play


    override fun onCreate() {
        super.onCreate()
        AudioPlayer.instance.addListener(this)
        AudioPlayer.instance.play(audioUrl)
        isPlaying = true
        playIv?.setOnClickListener {
            Logger.i("当前是否在播放：${AudioPlayer.instance.isPlaying()}")
            if (AudioPlayer.instance.isPlaying()) {
                playIv?.setImageResource(R.mipmap.pause_icon)
                AudioPlayer.instance.pause()
            } else {
                isPlaying = true
                playIv?.setImageResource(R.mipmap.play_icon)
                AudioPlayer.instance.start()
            }
        }

        closeIv?.setOnClickListener {
            AudioPlayer.instance.stop()
            dismiss()

        }


        startTv?.text = calculateTime(AudioPlayer.instance.getPosition() / 1000)
        endTv?.text = calculateTime(duration / 1000)
        Logger.i("最大值：$duration")
        seekBar?.max = duration
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val position = AudioPlayer.instance.getPosition()           // 获取当前播放的位置
                startTv?.text = calculateTime(position / 1000)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                isSeekbarChaning = true
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                isSeekbarChaning = false
                AudioPlayer.instance.seekTo(seekBar?.progress ?: 0)

            }
        })
        initDuration()
    }

    private fun initDuration() {
        lifecycleScope.launch(Dispatchers.IO) {
            while (isPlaying) {
                delay(800)
                if (isPlaying) {
                    launch(Dispatchers.Main) {
                        val progress = AudioPlayer.instance.getPosition()
                        if (!isSeekbarChaning) {
                            seekBar?.progress = progress
                        }
                        Logger.i("设置播放进度：$progress")
                    }

                }
            }
        }

    }


    /**
     * 设置播放的地址
     */
    fun setPlayUrl(url: String): AudioPlayDialog = apply {
        this.audioUrl = url
    }

    //计算播放时间
    fun calculateTime(time: Int): String? {
        val minute: Int
        val second: Int
        if (time > 60) {
            minute = time / 60
            second = time % 60
            //分钟再0~9
            return if (minute >= 0 && minute < 10) {
                //判断秒
                if (second >= 0 && second < 10) {
                    "0$minute:0$second"
                } else {
                    "0$minute:$second"
                }
            } else {
                //分钟大于10再判断秒
                if (second >= 0 && second < 10) {
                    "$minute:0$second"
                } else {
                    "$minute:$second"
                }
            }
        } else if (time < 60) {
            second = time
            return if (second >= 0 && second < 10) {
                "00:0$second"
            } else {
                "00:$second"
            }
        }
        return null
    }

    override fun onDismiss() {
        super.onDismiss()
        AudioPlayer.instance.removeListener(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        AudioPlayer.instance.removeListener(this)
    }

    override fun onAudioPlayerStart(duration: Int) {
        this.duration = duration
        endTv?.text = calculateTime(duration / 1000)
        seekBar?.max = duration
    }

    override fun onAudioPlayerStop() {
        isPlaying = false
        AudioPlayer.instance.stop()
        mAudioPlayerEndListener?.invoke()
        dismiss()
    }

    override fun onAudioPause() {
        isPlaying = false
        playIv?.setImageResource(R.mipmap.pause_icon)
        Logger.i("音频暂停")
    }

    override fun onAudioRestart() {
        isPlaying = true
        playIv?.setImageResource(R.mipmap.play_icon)
        Logger.i("音频继续播放")
        initDuration()
    }


    private var mAudioPlayerEndListener: (() -> Unit)? = null

    fun setAudioPLayerEndListener(listener: (() -> Unit)): AudioPlayDialog = apply {
        this.mAudioPlayerEndListener = listener
    }
}