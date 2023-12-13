package com.mj.preventbullying.client.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.databinding.ItemDeviceListBinding
import com.mj.preventbullying.client.http.result.DeviceRecord

/**
 * Create by MJ on 2023/12/9.
 * Describe :设备列表适配器
 */
class DeviceListAdapter : BaseQuickAdapter<DeviceRecord, DeviceListAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: ItemDeviceListBinding = ItemDeviceListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: DeviceRecord?) {
        //设置item数据
        holder.binding.apply {
            deviceNameTv.text = "名称：${item?.name}"
            deviceTypeTv.text = "型号：${item?.modelCode}"
            deviceSnTv.text = "SN：${item?.snCode}"
            deviceStatusTv.text = when (item?.state?.toInt()) {
                1 -> {
                    layoutItem.shapeDrawableBuilder.setSolidColor(context.getColor(com.sjb.base.R.color.device_offline))
                        .intoBackground()
                    "状态：离线"
                }

                2 -> {
                    layoutItem.shapeDrawableBuilder.setSolidColor(context.getColor(com.sjb.base.R.color.device_online))
                        .intoBackground()
                    "状态：在线"
                }

                else -> {
                    "状态：报警"
                }
            }
            deviceLocationTv.text = "位置：${item?.location}"
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        // 返回一个 ViewHolder
        return VH(parent)
    }
}