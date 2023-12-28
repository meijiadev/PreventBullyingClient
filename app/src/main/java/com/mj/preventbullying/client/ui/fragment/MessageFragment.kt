package com.mj.preventbullying.client.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.databinding.FragmentMessageBinding
import com.mj.preventbullying.client.http.result.Record
import com.mj.preventbullying.client.tool.AudioPlayer
import com.mj.preventbullying.client.ui.adapter.MessageAdapter
import com.mj.preventbullying.client.ui.adapter.PENDING_STATUS
import com.mj.preventbullying.client.ui.adapter.PROCESSED_IGNORE
import com.mj.preventbullying.client.ui.adapter.PROCESSED_STATUS
import com.mj.preventbullying.client.ui.adapter.PROCESSING_STATUS
import com.mj.preventbullying.client.ui.dialog.AudioPlayDialog
import com.mj.preventbullying.client.ui.dialog.MessageProcessDialog
import com.mj.preventbullying.client.ui.viewmodel.MessageViewModel
import com.mj.preventbullying.client.webrtc.getUUID
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */
class MessageFragment : BaseMvFragment<FragmentMessageBinding, MessageViewModel>() {
    private var messageAdapter: MessageAdapter? = null
    private var messageList: List<Record>? = null
    private var processPosition: Int? = null
    private var currentRecordId: String? = null
    private var curShowType = PENDING_STATUS


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
                MessageProcessDialog(requireContext())
                    .setToId(snCode)
                    .setClickListener(object :
                        MessageProcessDialog.MessageDialogClick {
                        override fun toCall() {
                            lifecycleScope.launch(Dispatchers.Main) {
                                MyApp.webrtcSocketManager.createWebrtcSc(
                                    SpManager.getString(Constant.USER_ID_KEY),
                                    snCode, getUUID()
                                )
                            }
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
            XPopup.Builder(requireContext()).isViewMode(true)
                .isDestroyOnDismiss(true)
                .dismissOnBackPressed(false)
                .dismissOnTouchOutside(false)
                .popupAnimation(PopupAnimation.TranslateFromBottom)
                .asCustom(messageProcessDialog)
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
    @SuppressLint("NotifyDataSetChanged")
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
        //  AudioPlayer.instance.addListener(this)
    }


    override fun onResume() {
        super.onResume()

    }

    override fun onStop() {
        super.onStop()
        AudioPlayer.instance.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        //  AudioPlayer.instance.removeListener(this)
        AudioPlayer.instance.stop()
    }

    override fun initListener() {
        viewModel.messageEvent.observe(this) {
            binding.smartRefreshLayout.finishRefresh(1000)
            messageList = it?.data?.records
            filtrationMsgTp(curShowType)
        }

        viewModel.getPreVieUrlEvent.observe(this) {
            it?.let { it1 ->
                it1.data?.let { data ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        kotlin.runCatching {
                            val audioPlayDialog = AudioPlayDialog(requireContext()).setPlayUrl(data.url)
                                .setAudioPLayerEndListener {
                                    currentRecordId?.let { recordId ->
                                        viewModel.recordProcess(
                                            recordId, "已查看报警现场音频", PROCESSED_STATUS
                                        )
                                    }
                                }
                            XPopup.Builder(requireContext())
                                .isViewMode(true)
                                .dismissOnBackPressed(false)
                                .dismissOnTouchOutside(false)
                                .isDestroyOnDismiss(true)
                                .popupAnimation(PopupAnimation.TranslateFromBottom)
                                .asCustom(audioPlayDialog)
                                .show()
                        }.onFailure {
                            Logger.e("error:${it}")
                        }
                    }
                }
            }
        }
        // 收到极光通知
        MyApp.globalEventViewModel.notifyMsgEvent.observe(this) {
            viewModel.getAllDeviceRecords()
            toast("收到报警推送")
        }
    }


}
