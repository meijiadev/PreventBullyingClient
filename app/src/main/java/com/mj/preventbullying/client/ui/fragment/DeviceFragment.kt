package com.mj.preventbullying.client.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.databinding.FragmentDeviceBinding
import com.mj.preventbullying.client.ui.adapter.DeviceListAdapter
import com.sjb.base.base.BaseMvFragment

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */

class DeviceFragment : BaseMvFragment<FragmentDeviceBinding, DeviceViewModel>() {
    private var deviceListAdapter: DeviceListAdapter? = null

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
        //deviceListAdapter?.addAll(deviceList)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.deviceList.layoutManager = layoutManager
        binding.deviceList.adapter = deviceListAdapter
    }

    override fun initViewObservable() {

    }

    override fun initView() {

    }

    override fun initListener() {
        viewModel.deviceResultEvent.observe(this) {
            deviceListAdapter?.addAll(it.data.records)
        }

    }
}