package com.mj.preventbullying.client.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.databinding.FragmentDeviceBinding
import com.mj.preventbullying.client.http.result.DeviceRecord
import com.mj.preventbullying.client.ui.activity.AddDeviceActivity
import com.mj.preventbullying.client.ui.adapter.DeviceListAdapter
import com.mj.preventbullying.client.ui.dialog.MessageTipsDialog
import com.mj.preventbullying.client.ui.viewmodel.DeviceViewModel
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvFragment

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */

class DeviceFragment : BaseMvFragment<FragmentDeviceBinding, DeviceViewModel>() {
    private var deviceListAdapter: DeviceListAdapter? = null
    private var deviceList: MutableList<DeviceRecord>? = null
    private var isHideFragment: Boolean = true


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
    }

    override fun initData() {
        deviceListAdapter = DeviceListAdapter()
        deviceListAdapter?.setItemAnimation(BaseQuickAdapter.AnimationType.ScaleIn)
        //deviceListAdapter?.addAll(deviceList)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.deviceList.layoutManager = layoutManager
        binding.deviceList.adapter = deviceListAdapter
    }

    override fun initViewObservable() {
        deviceListAdapter?.addOnItemChildClickListener(R.id.delete_iv) { adapter, view, position ->
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

        }
        deviceListAdapter?.addOnItemChildClickListener(R.id.amend_iv) { adapter, view, position ->
            val devMsg = deviceList?.get(position)
            val intent = Intent(mActivity, AddDeviceActivity::class.java)
            intent.putExtra("isAddDevice", false)
            intent.putExtra("snCode", devMsg?.snCode)
            intent.putExtra("deviceType", devMsg?.modelCode)
            intent.putExtra("location", devMsg?.location)
            intent.putExtra("desc", devMsg?.description)
            intent.putExtra("deviceId", devMsg?.deviceId)
            startActivity(intent)
        }

        deviceListAdapter?.addOnItemChildClickListener(R.id.upgrade_device_iv) { adapter, view, position ->
            val tipsDialog = MessageTipsDialog(requireContext()).setListener(object :
                MessageTipsDialog.OnListener {
                override fun onCancel() {

                }

                override fun onConfirm() {
                    val deviceId = deviceList?.get(position)?.deviceId?.toLong()
                    deviceId?.let {
                        viewModel.upgradeDev(deviceId)
                    }
                }
            }).setTitle("是否要给该设备系统升级？")
            XPopup.Builder(requireContext())
                .isViewMode(true)
                .popupAnimation(PopupAnimation.TranslateFromBottom)
                .asCustom(tipsDialog)
                .show()
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

        viewModel.amendDevEvent.observe(this) {
            if (it) {
                viewModel.getAllDevices()
            } else {
                toast("设备修改失败，请重试")
            }
        }

        MyApp.globalEventViewModel.notifyMsgEvent.observe(this) {
            viewModel.getAllDevices()
        }

        MyApp.globalEventViewModel.orgEvent.observe(this) {
            if (!isHideFragment) {
                viewModel.getAllDevices()
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Logger.i("是否隐藏：$hidden")
        isHideFragment = hidden
        if (!hidden) {
            viewModel.getAllDevices()
        }
    }

    override fun onResume() {
        if (!isFirstFragment)
            viewModel.getAllDevices()
        super.onResume()
        //Logger.i("onResume")
    }
}