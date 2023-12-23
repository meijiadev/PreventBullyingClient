package com.mj.preventbullying.client.ui.fragment

import android.content.Context
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.databinding.FragmentMessageBinding
import com.mj.preventbullying.client.http.result.Record
import com.mj.preventbullying.client.tool.AudioPlayer
import com.mj.preventbullying.client.ui.adapter.MessageAdapter
import com.mj.preventbullying.client.ui.adapter.PENDING_STATUS
import com.mj.preventbullying.client.ui.adapter.PROCESSED_IGNORE
import com.mj.preventbullying.client.ui.adapter.PROCESSED_STATUS
import com.mj.preventbullying.client.ui.adapter.PROCESSING_STATUS
import com.mj.preventbullying.client.ui.dialog.MessageProcessDialog
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */
class MessageFragment : BaseMvFragment<FragmentMessageBinding, MessageViewModel>(),
    AudioPlayer.AudioPlayerListener {
    private var messageAdapter: MessageAdapter? = null
    private var messageList: List<Record>? = null
    private var processPosition: Int? = null
    private var currentRecordId: String? = null
    private var curShowType = "null"


    companion object {
        fun newInstance(): MessageFragment {
            val args = Bundle()
            val fragment = MessageFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater, parent: ViewGroup?
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
            currentRecordId = messageList?.get(position)?.recordId
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
                        currentRecordId?.let {
                            viewModel.recordProcess(
                                it, "直接忽略", PROCESSED_IGNORE
                            )
                        }
                    }

                    override fun callFinish() {
                        currentRecordId?.let {
                            viewModel.recordProcess(
                                it, "已拨打设备语音了解情况", PROCESSED_STATUS
                            )
                        }
                    }
                })
            XPopup.Builder(requireContext()).isViewMode(true).isDestroyOnDismiss(true)
                .dismissOnBackPressed(true).dismissOnTouchOutside(false)
                .popupAnimation(PopupAnimation.TranslateFromBottom).asCustom(messageProcessDialog)
                .show()
        }
        binding.allMessageTv.setOnClickListener {
            resetMessageBt()
            binding.allMessageTv.shapeDrawableBuilder.setSolidColor(requireContext().getColor(com.sjb.base.R.color.gold))
                .intoBackground()
            filtrationMsgTp("null")

        }

        binding.pendingTv.setOnClickListener {
            resetMessageBt()
            binding.pendingTv.shapeDrawableBuilder.setSolidColor(requireContext().getColor(com.sjb.base.R.color.gold))
                .intoBackground()
            filtrationMsgTp(PENDING_STATUS)
        }

        binding.processedTv.setOnClickListener {
            resetMessageBt()
            binding.processedTv.shapeDrawableBuilder.setSolidColor(requireContext().getColor(com.sjb.base.R.color.gold))
                .intoBackground()
            filtrationMsgTp(PROCESSED_STATUS)
        }

        binding.ignoreTv.setOnClickListener {
            resetMessageBt()
            binding.ignoreTv.shapeDrawableBuilder.setSolidColor(requireContext().getColor(com.sjb.base.R.color.gold))
                .intoBackground()
            filtrationMsgTp(PROCESSED_IGNORE)

        }

        binding.processingTv.setOnClickListener {
            resetMessageBt()
            binding.processingTv.shapeDrawableBuilder.setSolidColor(requireContext().getColor(com.sjb.base.R.color.gold))
                .intoBackground()
            filtrationMsgTp(PROCESSING_STATUS)
        }
    }

    private fun resetMessageBt() {
        binding.apply {
            allMessageTv.shapeDrawableBuilder.setSolidColor(requireContext().getColor(R.color.white))
                .intoBackground()
            pendingTv.shapeDrawableBuilder.setSolidColor(requireContext().getColor(R.color.white))
                .intoBackground()
            processedTv.shapeDrawableBuilder.setSolidColor(requireContext().getColor(R.color.white))
                .intoBackground()
            ignoreTv.shapeDrawableBuilder.setSolidColor(requireContext().getColor(R.color.white))
                .intoBackground()
            processingTv.shapeDrawableBuilder.setSolidColor(requireContext().getColor(R.color.white))
                .intoBackground()
        }
    }

    /**
     * 按照类型过滤
     */
    private fun filtrationMsgTp(type: String) {
        curShowType = type
        if (type == "null") {
            messageAdapter?.submitList(messageList)
            messageAdapter?.notifyDataSetChanged()
            return
        }
        val list = mutableListOf<Record>()
        messageList?.let {
            for (record in it) {
                if (record.state == type) {
                    list.add(record)
                }
            }
            messageAdapter?.submitList(list)
            messageAdapter?.notifyDataSetChanged()
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
            filtrationMsgTp(curShowType)
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


    private var isPlaying = false
    override fun onAudioPlayerStart(duration: Int) {
        isPlaying = true
        Logger.i("播放开始")
        createFloatWindow()
        startTv?.text = calculateTime(AudioPlayer.instance.getPosition() / 1000)
        endTv?.text = calculateTime(duration / 1000)
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
        lifecycleScope.launch(Dispatchers.IO) {
            while (isPlaying) {
                delay(1000)
                launch(Dispatchers.Main) {
                    if (!isSeekbarChaning) seekBar?.progress = AudioPlayer.instance.getPosition()
                }
            }
        }
//        val timer = Timer()
//        timer.schedule(object : TimerTask() {
//            override fun run() {
//                if (!isSeekbarChaning) seekBar?.progress = AudioPlayer.instance.getPosition()
//            }
//        }, 0, 1000)

    }

    override fun onAudioPlayerStop() {
        Logger.i("播放结束")
        isPlaying = false
        AudioPlayer.instance.stop()
        windowManager?.removeView(floatRootView)
        currentRecordId?.let {
            viewModel.recordProcess(
                it, "已查看报警现场音频", PROCESSED_STATUS
            )
        }

    }

    private var windowManager: WindowManager? = null
    private var isSeekbarChaning = false
    private var seekBar: SeekBar? = null
    private var startTv: TextView? = null
    private var endTv: TextView? = null
    private var floatRootView: View? = null
    private var playIv: AppCompatImageView? = null
    private var closeIv: AppCompatImageView? = null

    /**
     * 创建悬浮窗
     */
    private fun createFloatWindow() {
        //2、设置悬浮窗的初始位置和参数
        val layoutParam = WindowManager.LayoutParams().apply {
            //设置大小 自适应
            width = WRAP_CONTENT
            height = WRAP_CONTENT
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            format = PixelFormat.RGBA_8888
        }
        floatRootView = LayoutInflater.from(context).inflate(R.layout.dialog_audio_play, null)
        seekBar = floatRootView?.findViewById(R.id.seekbar)
        startTv = floatRootView?.findViewById(R.id.tv_start)
        endTv = floatRootView?.findViewById(R.id.tv_end)
        playIv = floatRootView?.findViewById(R.id.play_iv)
        closeIv = floatRootView?.findViewById(R.id.close_iv)
        // 4. 获取WindowManager并将悬浮窗添加到窗口
        windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager?.addView(floatRootView, layoutParam)
        playIv?.setOnClickListener {
            Logger.i("当前是否在播放：${AudioPlayer.instance.isPlaying()}")
            if (AudioPlayer.instance.isPlaying()) {
                playIv?.setImageResource(R.mipmap.pause_icon)
                AudioPlayer.instance.pause()
            } else {
                playIv?.setImageResource(R.mipmap.play_icon)
                AudioPlayer.instance.start()
            }
        }

        closeIv?.setOnClickListener {
            isPlaying = false
            AudioPlayer.instance.stop()
            windowManager?.removeView(floatRootView)
            currentRecordId?.let {
                viewModel.recordProcess(
                    it, "已查看报警现场音频", PROCESSED_STATUS
                )
            }
        }
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
