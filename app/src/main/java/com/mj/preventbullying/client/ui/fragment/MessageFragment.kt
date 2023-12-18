package com.mj.preventbullying.client.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.hjq.window.EasyWindow
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.databinding.FragmentMessageBinding
import com.mj.preventbullying.client.http.result.Record
import com.mj.preventbullying.client.tool.AudioPlayer
import com.mj.preventbullying.client.ui.adapter.MessageAdapter
import com.mj.preventbullying.client.ui.adapter.PROCESSED_IGNORE
import com.mj.preventbullying.client.ui.adapter.PROCESSED_STATUS
import com.mj.preventbullying.client.ui.dialog.MessageProcessDialog
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */
class MessageFragment : BaseMvFragment<FragmentMessageBinding, MessageViewModel>(),
    AudioPlayer.AudioPlayerListener {
    private var messageAdapter: MessageAdapter? = null
    private var messageList: List<Record>? = null
    private var processPosition: Int? = null

    companion object {
        fun newInstance(): MessageFragment {
            val args = Bundle()
            val fragment = MessageFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentMessageBinding {
        return FragmentMessageBinding.inflate(inflater, parent, false)
    }


    override fun initParam() {
        viewModel.getAllDeviceRecords()
    }

    override fun initData() {
        messageAdapter = MessageAdapter()
        messageAdapter?.setItemAnimation(BaseQuickAdapter.AnimationType.SlideInLeft)
        //deviceListAdapter?.addAll(deviceList)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.messageList.layoutManager = layoutManager
        binding.messageList.adapter = messageAdapter
    }

    override fun initViewObservable() {
        messageAdapter?.addOnItemChildClickListener(R.id.go_process_tv) { adapter, view, position ->
            processPosition = position
            val snCode = messageList?.get(position)?.snCode
            val recordId = messageList?.get(position)?.recordId
            val fileId = messageList?.get(position)?.fileId
            Logger.i("去处理消息")
            val messageProcessDialog =
                MessageProcessDialog(requireContext()).setClickListener(object :
                    MessageProcessDialog.MessageDialogClick {
                    override fun toCall() {
                        MyApp.socketEventViewModel.call(snCode)
                    }

                    override fun playWarnAudio() {
                        fileId?.let {
                            viewModel.getAudioPreUrl(fileId)
                        }
                    }

                    override fun ignore() {
                        recordId?.let {
                            viewModel.recordProcess(
                                it, "直接忽略",
                                PROCESSED_IGNORE
                            )
                        }
                    }

                    override fun callFinish() {
                        recordId?.let {
                            viewModel.recordProcess(
                                it, "已拨打设备语音了解情况",
                                PROCESSED_STATUS
                            )
                        }
                    }
                })
            XPopup.Builder(requireContext()).isViewMode(true).isDestroyOnDismiss(true)
                .dismissOnBackPressed(true).dismissOnTouchOutside(false)
                .popupAnimation(PopupAnimation.TranslateFromBottom).asCustom(messageProcessDialog)
                .show()
        }
    }

    override fun initView() {
        binding.smartRefreshLayout.setOnRefreshListener {
            Logger.i("下拉刷新")
            it.setReboundDuration(300)
            viewModel.getAllDeviceRecords()
        }
        AudioPlayer.instance.addListener(this)
    }


    override fun onStop() {
        super.onStop()
        AudioPlayer.instance.removeListener(this)
        AudioPlayer.instance.stop()
    }

    override fun initListener() {
        viewModel.messageEvent.observe(this) {
            binding.smartRefreshLayout.finishRefresh(1000, true, true)
            messageList = it?.data?.records
            messageAdapter?.submitList(it.data.records)
        }

        viewModel.getPreVieUrlEvent.observe(this) {
            it?.let { it1 ->
                it1.data?.let { data ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        //PcmPlayer.play(data.url)
                        // VlcMusicPlayer.instance.playNet(Uri.parse(data.url))
                        kotlin.runCatching {
                            AudioPlayer.instance.play(data.url)
                        }.onFailure {
                            Logger.e("error:${it}")
                        }
                    }
                }
            }
        }
    }

    override fun onAudioPlayerStart() {
        Logger.i("播放开始")
        val seekBar: SeekBar =
            EasyWindow.with(activity).setContentView(R.layout.dialog_audio_play)
                .findViewById<ProgressBar>(R.id.seekbar) as SeekBar
        val startTv = EasyWindow.with(activity).setContentView(R.layout.dialog_audio_play)
            .findViewById<TextView>(R.id.tv_start) as TextView
        val endTv = EasyWindow.with(activity).setContentView(R.layout.dialog_audio_play)
            .findViewById<TextView>(R.id.tv_end) as TextView
        seekBar.progress = 0
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val duration = AudioPlayer.instance.getDuration() / 1000    // 获取音频总时长
                val position = AudioPlayer.instance.getPosition()           // 获取当前播放的位置
                startTv.text = calculateTime(position / 1000)
                endTv.text = calculateTime(duration)

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        EasyWindow.with(activity)
            .setContentView(R.layout.dialog_audio_play)
            .setOnClickListener(
                R.id.close_iv,
                EasyWindow.OnClickListener<AppCompatImageView?> { easyWindow: EasyWindow<*>, view: AppCompatImageView? ->
                    easyWindow.cancel()
                    AudioPlayer.instance.stop()
                })
            .setOnClickListener(
                R.id.play_iv,
                EasyWindow.OnClickListener { easyWindow: EasyWindow<*>, view: AppCompatImageView? ->
                    // easyWindow.cancel()
                    if (AudioPlayer.instance.isPlaying()) {
                        AudioPlayer.instance.pause()
                    } else {
                        AudioPlayer.instance.start()
                    }
                }
            )
            .show()


    }

    override fun onAudioPlayerStop() {
        Logger.i("播放结束")
        AudioPlayer.instance.stop()
        EasyWindow.cancelAll()
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

}
