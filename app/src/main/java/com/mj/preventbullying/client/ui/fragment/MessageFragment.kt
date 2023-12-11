package com.mj.preventbullying.client.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.BaseQuickAdapter.OnItemClickListener
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.databinding.FragmentMessageBinding
import com.mj.preventbullying.client.http.result.Record
import com.mj.preventbullying.client.ui.adapter.MessageAdapter
import com.mj.preventbullying.client.ui.dialog.MessageProcessDialog
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvFragment

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */
class MessageFragment : BaseMvFragment<FragmentMessageBinding, MessageViewModel>() {
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
        if (Constant.userId != null && Constant.registerId != null) {
            MyApp.socketEventViewModel.initSocket(Constant.userId!!, Constant.registerId!!)
            // viewModel.getAllDeviceRecords()
            viewModel.getAllDeviceRecords()
        }
    }

    override fun initData() {
        messageAdapter = MessageAdapter()
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
            Logger.i("去处理消息")
            val messageProcessDialog =
                MessageProcessDialog(requireContext()).setClickListener(object :
                    MessageProcessDialog.MessageDialogClick {
                    override fun toCall() {
                        MyApp.socketEventViewModel.call(snCode)

                    }

                    override fun playWarnAudio() {

                    }

                    override fun ignore() {

                    }
                })
            XPopup.Builder(requireContext()).isViewMode(true).isDestroyOnDismiss(true)
                .dismissOnBackPressed(true).dismissOnTouchOutside(false)
                .popupAnimation(PopupAnimation.TranslateFromBottom).asCustom(messageProcessDialog)
                .show()
        }
    }

    override fun initView() {

    }

    override fun initListener() {
        viewModel.messageEvent.observe(this) {
            messageList = it.data.records
            messageAdapter?.addAll(it.data.records)
        }
    }
}