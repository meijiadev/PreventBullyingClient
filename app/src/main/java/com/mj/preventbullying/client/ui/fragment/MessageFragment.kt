package com.mj.preventbullying.client.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.jpush.android.api.JPushInterface
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.app.MyApp
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
import com.mj.preventbullying.client.ui.dialog.InputMsgDialog
import com.mj.preventbullying.client.ui.dialog.MessageProcessDialog
import com.mj.preventbullying.client.ui.viewmodel.MessageViewModel
import com.mj.preventbullying.client.webrtc.getUUID
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */
class MessageFragment : BaseMvFragment<`FragmentMessageBinding`, MessageViewModel>() {
    private var messageAdapter: MessageAdapter? = null
    private var loadMoreHelp: QuickAdapterHelper? = null
    private var messageList: MutableList<Record> = mutableListOf()
    private var processPosition: Int? = null
    private var currentRecordId: String? = null
    private var curShowType = PENDING_STATUS
    private var isHideFragment: Boolean = false
    private var isNotify = false

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
            viewModel.getAllDeviceRecords(1)
        }
    }

    override fun initData() {
        messageAdapter = MessageAdapter()
        //messageAdapter?.setItemAnimation(BaseQuickAdapter.AnimationType.ScaleIn)
        //deviceListAdapter?.addAll(deviceList)
        loadMoreHelp = QuickAdapterHelper.Builder(messageAdapter!!)
            .setTrailingLoadStateAdapter(object : TrailingLoadStateAdapter.OnTrailingListener {
                override fun onFailRetry() {
                    Logger.i("加载更多失败")
                    viewModel.getAllDeviceRecords(curDataPage + 1)
                }

                override fun onLoad() {
                    Logger.i("加载更多数据")
                    viewModel.getAllDeviceRecords(curDataPage + 1)
                }

//                override fun isAllowLoading(): Boolean {
//                    return binding.smartRefreshLayout.isRefreshing
//                }

            }).build()
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.messageList.layoutManager = layoutManager
        binding.messageList.adapter = loadMoreHelp?.adapter
    }

    override fun initViewObservable() {
        // 对讲
        messageAdapter?.addOnItemChildClickListener(R.id.call_tv) { adapter, view, position ->
            processPosition = position
            val record=messageAdapter?.getItem(position)
            Logger.i("当前点击的参数：$position,${record}")
            val snCode = record?.snCode
            currentRecordId = record?.recordId
            //val fileId = messageList?.get(position)?.fileId
            Logger.i("去处理消息")
            lifecycleScope.launch(Dispatchers.Main) {
                MyApp.webrtcSocketManager.createWebrtcSc(
                    SpManager.getString(Constant.USER_ID_KEY),
                    snCode, getUUID()
                )
            }
            val messageProcessDialog =
                MessageProcessDialog(requireContext())
                    .setToId(snCode)
            XPopup.Builder(requireContext()).isViewMode(true)
                .isDestroyOnDismiss(true)
                .dismissOnBackPressed(false)
                .dismissOnTouchOutside(false)
                .popupAnimation(PopupAnimation.TranslateFromBottom)
                .asCustom(messageProcessDialog)
                .show()

        }

        // 播放
        messageAdapter?.addOnItemChildClickListener(R.id.play_tv) { adapter, view, position ->
            processPosition = position
            //val snCode = messageList?.get(position)?.snCode
            val record=messageAdapter?.getItem(position)
            currentRecordId = record?.recordId
            val fileId = record?.fileId
            fileId?.let {
                viewModel.getAudioPreUrl(fileId)
            }
        }
        // 处理按钮
        messageAdapter?.addOnItemChildClickListener(R.id.process_bt) { adapter, view, position ->
            processPosition = position
            val record=messageAdapter?.getItem(position)
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
        messageList.let {
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
            curDataPage = 1
            viewModel.getAllDeviceRecords(curDataPage)
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
                    messageList = datas
                } else {
                    messageList.addAll(datas)
                }
            }
            binding.smartRefreshLayout.finishRefresh(500)
            filtrationMsgTp(curShowType)
            if (!isHideFragment) {
                // 可见状态
                JPushInterface.clearAllNotifications(context)
                isNotify = false
            }
        }

        viewModel.getPreVieUrlEvent.observe(this) {
            it?.let { it1 ->
                it1.data?.let { data ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        kotlin.runCatching {
                            val audioPlayDialog =
                                AudioPlayDialog(requireContext()).setPlayUrl(data.url)
                                    .setAudioPLayerEndListener {

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
            curDataPage = 1
            viewModel.getAllDeviceRecords(curDataPage)
            Logger.i("收到报警通知")
            isNotify = true
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Logger.i("是否隐藏：$hidden")
        if (!hidden) {
            viewModel.getAllDeviceRecords(curDataPage)
        }
        isHideFragment = hidden
        if (!isHideFragment) {
            isNotify = false
            JPushInterface.clearAllNotifications(context)
        }
    }


}
