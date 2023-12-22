package com.mj.preventbullying.client.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.databinding.FragmentDeviceBinding
import com.mj.preventbullying.client.http.result.DeviceRecord
import com.mj.preventbullying.client.ui.MainViewModel
import com.mj.preventbullying.client.ui.adapter.DeviceListAdapter
import com.mj.preventbullying.client.ui.dialog.MessageTipsDialog
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */

class DeviceFragment : BaseMvFragment<FragmentDeviceBinding, DeviceViewModel>() {
    private var deviceListAdapter: DeviceListAdapter? = null
    private var deviceList: MutableList<DeviceRecord>? = null

    private var mainViewModel: MainViewModel? = null

    companion object {
        fun newInstance(): DeviceFragment {
            val args = Bundle()
            val fragment = DeviceFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentDeviceBinding {
        return FragmentDeviceBinding.inflate(inflater, parent, false)
    }

    override fun initParam() {
        viewModel.getAllDevices()
        mainViewModel = getActivityViewModel(MainViewModel::class.java)

    }

    override fun initData() {
        deviceListAdapter = DeviceListAdapter()
        //deviceListAdapter?.addAll(deviceList)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.deviceList.layoutManager = layoutManager
        binding.deviceList.adapter = deviceListAdapter
    }

    override fun initViewObservable() {
        deviceListAdapter?.setOnItemLongClickListener { adapter, view, position ->
            val tipsDialog = MessageTipsDialog(requireContext()).setTitle("是否确定删除该设备？")
                .setListener(object : MessageTipsDialog.OnListener {
                    override fun onCancel() {

                    }

                    override fun onConfirm() {
                        runCatching {
                            val deviceId = deviceList?.get(position)?.deviceId?.toLong()
                            if (deviceId != null) {
                                viewModel.deleteDev(deviceId)
                            } else {
                                toast("删除失败！")
                            }
                        }.onFailure {
                            toast("删除错误！")
                        }
                    }
                })
            XPopup.Builder(requireContext()).isViewMode(true)
                .popupAnimation(PopupAnimation.TranslateFromBottom).asCustom(tipsDialog).show()
            true
        }

    }

    override fun initView() {
        binding.smartRefreshLayout.let {
            it.setReboundDuration(300)
            it.setOnRefreshListener {
                Logger.i("下拉刷新")
                viewModel.getAllDevices()
            }
        }

    }

    override fun initListener() {
        viewModel.deviceResultEvent.observe(this) {
            deviceList = it.data.records
            binding.smartRefreshLayout.finishRefresh(1000)
            deviceListAdapter?.submitList(deviceList)
        }

        viewModel.deleteDevEvent.observe(this) {
            if (it) {
                viewModel.getAllDevices()
            }
        }
        mainViewModel?.addDevEvent?.observe(this) {
            if (it) {
                viewModel.getAllDevices()
            }

        }
    }
}