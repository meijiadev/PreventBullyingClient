package com.mj.preventbullying.client.ui.activity

import android.annotation.SuppressLint
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.app.AppMvActivity
import com.mj.preventbullying.client.databinding.ActivityAlarmAudioBinding
import com.mj.preventbullying.client.tool.AudioPlayer
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.tool.getAssetsList
import com.mj.preventbullying.client.ui.adapter.AlarmAudioAdapter
import com.sjb.base.base.BaseMvActivity
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2024/1/3.
 * Describe : 警告提示音选择界面
 */

class AlarmAudioActivity : AppMvActivity<ActivityAlarmAudioBinding, BaseViewModel>(),
    AudioPlayer.AudioPlayerListener {
    private var audioAdapter: AlarmAudioAdapter? = null
    private var alarmAudios: List<String>? = null
    private var curPosition: Int? = null
    private var curAlarmName: String? = null

    override fun getViewBinding(): ActivityAlarmAudioBinding {
        return ActivityAlarmAudioBinding.inflate(layoutInflater)
    }

    override fun initParam() {

    }

    override fun initData() {
        alarmAudios = getAssetsList()
        audioAdapter = AlarmAudioAdapter()
        var selectAlarm =
            SpManager.getString(Constant.ALARM_PLAY_NAME_KEY)
        if (selectAlarm.isNullOrEmpty()) {
            selectAlarm = Constant.alarmAudioName
        }
        audioAdapter?.setSelectAlarm(selectAlarm)
        val layoutManager = GridLayoutManager(this, 2)
        binding.alarmRecycler.layoutManager = layoutManager
        audioAdapter?.submitList(alarmAudios)
        binding.alarmRecycler.adapter = audioAdapter

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initViewObservable() {
        audioAdapter?.addOnItemChildClickListener(R.id.play_iv) { adapter, view, position ->
            val alarmName = alarmAudios?.get(position)
            if (curAlarmName == alarmName) {
                AudioPlayer.instance.stop()
            } else {
                alarmName?.let {
                    audioAdapter?.setPlayingName(it)
                    audioAdapter?.notifyDataSetChanged()
                    curPosition = position
                    curAlarmName = it
                    AudioPlayer.instance.playAssets(it)
                }

            }
        }
        audioAdapter?.setOnItemClickListener { adapter, view, position ->
            val selectAlarm = alarmAudios?.get(position)
            selectAlarm?.let {
                SpManager.putString(Constant.ALARM_PLAY_NAME_KEY, it)
                audioAdapter?.setSelectAlarm(it)
                audioAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun initView() {
        binding.titleLayout.titleTv.text = "消息提示音"
    }

    override fun initListener() {
        binding.titleLayout.backIv.setOnClickListener {
            //AudioPlayer.instance.release()
            AudioPlayer.instance.stop()
            finish()
        }
    }

    override fun onAudioPlayerStart(duration: Int) {
        curAlarmName?.let {
            audioAdapter?.setPlayingName(it)
        }
    }

    override fun onAudioPlayerStop() {
        curAlarmName = null
        audioAdapter?.setPlayingName(null)
        AudioPlayer.instance.stop()
    }

    override fun onAudioPause() {
        curAlarmName = null
        audioAdapter?.setPlayingName(null)
    }

    override fun onAudioRestart() {
        curAlarmName?.let {
            audioAdapter?.setPlayingName(it)
        }
    }


    override fun onStart() {
        super.onStart()
        AudioPlayer.instance.addListener(this)
    }

    override fun onStop() {
        super.onStop()
        AudioPlayer.instance.removeListener(this)
    }
}