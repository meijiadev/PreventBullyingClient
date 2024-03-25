package com.mj.preventbullying.client.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cn.jpush.android.api.JPushInterface
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.databinding.FragmentMessageBinding
import com.mj.preventbullying.client.tool.AudioPlayer
import com.mj.preventbullying.client.ui.adapter.MessageAdapter
import com.mj.preventbullying.client.ui.adapter.PENDING_STATUS
import com.mj.preventbullying.client.ui.adapter.PROCESSED_IGNORE
import com.mj.preventbullying.client.ui.adapter.PROCESSED_STATUS
import com.mj.preventbullying.client.ui.adapter.PROCESSING_STATUS
import com.mj.preventbullying.client.ui.dialog.AudioPlayDialog
import com.mj.preventbullying.client.ui.dialog.InputMsgDialog
import com.mj.preventbullying.client.ui.dialog.MessageProcessDialog
import com.mj.preventbullying.client.ui.viewmodel.MessageViewModel
import com.mj.preventbullying.client.webrtc.getUUID
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mj.preventbullying.client.http.result.Record
import com.mj.preventbullying.client.ui.activity.RtcVideoActivity
import com.mj.preventbullying.client.ui.dialog.MessageTipsDialog
import com.mj.preventbullying.client.ui.dialog.RtcVideoDialog

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */
class MessageFragment : BaseMvFragment<FragmentMessageBinding, MessageViewModel>() {
    private var messageAdapter: MessageAdapter? = null
    private var loadMoreHelp: QuickAdapterHelper? = null
    private var processPosition: Int? = null
    private var currentRecordId: String? = null
    private var currentState: String? = null
    private var curShowType: String? = PENDING_STATUS
    private var isHideFragment: Boolean = false
    private var isNotify = false
    private var audioPlayDialog: AudioPlayDialog? = null

    private var curDataPage = 1   //当前获取第几页数据
    private var maxPage = 1       // 最大页数


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
        lifecycleScope.launch {
            delay(200)
            viewModel.getAllDeviceRecords(1, curShowType)
        }
        binding.titleLayout.titleTv.text="告警消息"
    }

    override fun initData() {
        messageAdapter = MessageAdapter()
        messageAdapter?.setItemAnimation(BaseQuickAdapter.AnimationType.ScaleIn)
        //deviceListAdapter?.addAll(deviceList)
        loadMoreHelp = QuickAdapterHelper.Builder(messageAdapter!!)
            .setTrailingLoadStateAdapter(object : TrailingLoadStateAdapter.OnTrailingListener {
                override fun onFailRetry() {
                    Logger.i("加载更多失败")
                    viewModel.getAllDeviceRecords(curDataPage + 1, curShowType)
                }

                override fun onLoad() {
                    Logger.i("加载更多数据")
                    viewModel.getAllDeviceRecords(curDataPage + 1, curShowType)
                }
            }).build()
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.messageList.layoutManager = layoutManager
        binding.messageList.adapter = loadMoreHelp?.adapter
    }

    override fun initViewObservable() {
        // 对讲
        messageAdapter?.addOnItemChildClickListener(R.id.call_tv) { adapter, view, position ->
            XXPermissions.with(this).permission(Permission.RECORD_AUDIO)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                        if (all) {
                            processPosition = position
                            val record = messageAdapter?.getItem(position)
                            Logger.i("当前点击的参数：$position,${record}")
                            val snCode = record?.snCode
                            currentRecordId = record?.recordId
                            currentState = record?.state
                            //val fileId = messageList?.get(position)?.fileId
                            Logger.i("去处理消息")
                            currentRecordId?.let {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MyApp.webrtcSocketManager.createWebrtcSc(
                                        SpManager.getString(Constant.USER_ID_KEY),
                                        snCode, getUUID(),
                                        it
                                    )
                                }
                            }
                            val messageProcessDialog =
                                MessageProcessDialog(requireContext())
                                    .setToId(snCode).setCallListener {
                                    }
                            XPopup.Builder(requireContext()).isViewMode(true)
                                .isDestroyOnDismiss(true)
                                .dismissOnBackPressed(false)
                                .dismissOnTouchOutside(false)
                                .popupAnimation(PopupAnimation.TranslateFromBottom)
                                .asCustom(messageProcessDialog)
                                .show()
                        }
                    }

                    override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                        super.onDenied(permissions, never)
                        toast("请前往应用权限页面打开该应用的麦克风权限才能使用语音对讲功能！")
                    }
                })

        }

        // 播放
        messageAdapter?.addOnItemChildClickListener(R.id.play_tv) { adapter, view, position ->
            processPosition = position
            //val snCode = messageList?.get(position)?.snCode
            val record = messageAdapter?.getItem(position)
            currentRecordId = record?.recordId
            currentState = record?.state
            currentRecordId?.let {
                viewModel.getAudioPreUrl(it)
            }
        }
        // 处理按钮
        messageAdapter?.addOnItemChildClickListener(R.id.process_bt) { adapter, view, position ->
            processPosition = position
            val record = messageAdapter?.getItem(position)
            currentRecordId = record?.recordId
            val inputMsgDialog = InputMsgDialog(requireContext()).setConfirmListener { model, msg ->
                currentRecordId?.let { recordId ->
                    viewModel.recordProcess(
                        recordId, msg, model
                    )
                    Logger.i("处理活动：$msg")
                }
            }
            XPopup.Builder(requireContext())
                .isViewMode(true)
                .popupAnimation(PopupAnimation.TranslateFromBottom)
                .asCustom(inputMsgDialog)
                .show()
        }

        messageAdapter?.addOnItemChildClickListener(R.id.check_monitor_bt) { adapter, view, position ->
            processPosition = position
            val record = messageAdapter?.getItem(position)
            currentRecordId = record?.recordId
            currentRecordId?.let { viewModel.getRtcVideoUrl(it) }

        }



        binding.allMessageTv.setOnClickListener {
            resetMessageBt()
            binding.allMessageTv.shapeDrawableBuilder.setSolidColor(requireContext().getColor(com.sjb.base.R.color.gold))
                .intoBackground()
            filtrationMsgTp(null)

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
        binding.titleLayout.backIv.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun showRtcVideo(url: String) {
        val rtcVideoDialog = RtcVideoDialog(requireContext()).onFullListener {
            val intent = Intent(mActivity, RtcVideoActivity::class.java)
            intent.putExtra("videoUrl", url)
            startActivity(intent)
        }.setVideoUrl(url)
        XPopup.Builder(requireContext())
            .isViewMode(true)
            .isDestroyOnDismiss(true)
            .dismissOnBackPressed(false)
            .dismissOnTouchOutside(false)
            .popupAnimation(PopupAnimation.TranslateFromBottom)
            .asCustom(rtcVideoDialog)
            .show()
    }

    private fun showAudioDialog(url: String) {
        if (audioPlayDialog == null || audioPlayDialog?.isShow == false) {
            kotlin.runCatching {
                audioPlayDialog =
                    AudioPlayDialog(requireContext()).setPlayUrl(url)
                        .setAudioPLayerStartListener {
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
    private fun filtrationMsgTp(type: String?) {
        curShowType = type
        viewModel.getAllDeviceRecords(1, curShowType)
    }

    override fun initView() {
        binding.smartRefreshLayout.setOnRefreshListener {
            Logger.i("下拉刷新")
            it.setReboundDuration(300)
            curDataPage = 1
            viewModel.getAllDeviceRecords(curDataPage, curShowType)
        }
    }


    override fun onResume() {
        super.onResume()
        Logger.i("on resume")
    }

    override fun onStop() {
        super.onStop()
        AudioPlayer.instance.pause()
        Logger.i("on stop")
    }

    override fun onDestroy() {
        super.onDestroy()
        //  AudioPlayer.instance.removeListener(this)
        AudioPlayer.instance.stop()
        Logger.i("on destroy")
    }

    override fun initListener() {
        viewModel.messageEvent.observe(this) {
            if (it?.data?.records.isNullOrEmpty()) {
                curDataPage = 1
                val size = messageAdapter?.itemCount ?: 0
                if (size > 0) {
                    messageAdapter?.removeAtRange(0..<size)
                }
                binding.smartRefreshLayout.finishRefresh()
                loadMoreHelp?.trailingLoadState = LoadState.NotLoading(true)
                return@observe
            }
            curDataPage = it.data?.current ?: 1
            maxPage = it.data?.pages ?: 1
            if (curDataPage + 1 > maxPage) {
                loadMoreHelp?.trailingLoadState = LoadState.NotLoading(true)
            } else {
                loadMoreHelp?.trailingLoadState = LoadState.NotLoading(false)
            }
            if (!it?.data?.records.isNullOrEmpty()) {
                val datas = it?.data?.records as MutableList<Record>
                if (it.data.current == 1) {
                    messageAdapter?.submitList(datas)
                } else {
                    messageAdapter?.addAll(datas)
                }
            }
            binding.smartRefreshLayout.finishRefresh(500)
            if (!isHideFragment) {
                // 可见状态
                JPushInterface.clearAllNotifications(context)
                isNotify = false
            }
        }

        viewModel.getPreVieUrlEvent.observe(this) { it ->
            it?.let { it1 ->
                it1.data?.let { data ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        showAudioDialog(data.url)
                    }
                }
            }
        }
        // 收到极光通知
        MyApp.globalEventViewModel.notifyMsgEvent.observe(this) {
            curDataPage = 1
            curShowType = PENDING_STATUS
            viewModel.getAllDeviceRecords(curDataPage, curShowType)
            Logger.i("收到报警通知")
            isNotify = true
        }

        MyApp.globalEventViewModel.orgEvent.observe(this) {
            if (!isHideFragment) {
                viewModel.getAllDeviceRecords(1, curShowType)
            }

        }
        viewModel.rtcVideoUrlEvent.observe(this) {
            if (rtcTipsDialog == null || rtcTipsDialog?.isShow == false) {
                rtcTipsDialog =
                    MessageTipsDialog(requireContext()).setTitle("查看当前设备关联的监控画面？")
                        .setListener(object : MessageTipsDialog.OnListener {
                            override fun onCancel() {

                            }

                            override fun onConfirm() {
                                showRtcVideo(it)
                            }
                        })
                XPopup.Builder(requireContext()).isViewMode(true)
                    .popupAnimation(PopupAnimation.TranslateFromBottom).asCustom(rtcTipsDialog)
                    .show()
            }
        }
    }

    private var rtcTipsDialog: MessageTipsDialog? = null
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Logger.i("是否隐藏：$hidden")
        if (!hidden) {
            viewModel.getAllDeviceRecords(curDataPage, curShowType)
        }
        isHideFragment = hidden
        if (!isHideFragment) {
            isNotify = false
            JPushInterface.clearAllNotifications(context)
        }
    }


}
