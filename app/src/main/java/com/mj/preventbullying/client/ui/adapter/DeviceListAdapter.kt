package com.mj.preventbullying.client.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.LayoutAnimationController
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.mj.preventbullying.client.databinding.ItemDeviceListBinding

/**
 * Create by MJ on 2023/12/9.
 * Describe :
 */

class DeviceListAdapter : BaseQuickAdapter<DeviceBean, DeviceListAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: ItemDeviceListBinding = ItemDeviceListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: DeviceBean?) {
        //设置item数据
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        // 返回一个 ViewHolder
        return VH(parent)
    }
}