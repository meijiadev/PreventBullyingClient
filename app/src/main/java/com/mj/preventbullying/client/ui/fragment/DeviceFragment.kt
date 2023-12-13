package com.mj.preventbullying.client.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.databinding.FragmentDeviceBinding
import com.mj.preventbullying.client.ui.adapter.DeviceListAdapter
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
            binding.smartRefreshLayout.finishRefresh(1000)
            deviceListAdapter?.submitList(it.data.records)
        }

    }
}