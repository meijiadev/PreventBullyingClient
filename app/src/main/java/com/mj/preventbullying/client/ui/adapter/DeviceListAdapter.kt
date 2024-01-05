package com.mj.preventbullying.client.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.databinding.ItemDeviceListBinding
import com.mj.preventbullying.client.databinding.ItemDeviceNewListBinding
import com.mj.preventbullying.client.http.result.DeviceRecord

/**
 * Create by MJ on 2023/12/9.
 * Describe :设备列表适配器
 */
class DeviceListAdapter : BaseQuickAdapter<DeviceRecord, DeviceListAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: ItemDeviceNewListBinding = ItemDeviceNewListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: DeviceRecord?) {
        //设置item数据
        holder.binding.apply {
            nameTv.text = "校园防欺凌设备 ${item?.modelCode}"
            locationTv.text = "${item?.org?.name} | ${item?.location}"
            snTv.text = "SN-${item?.snCode}"
            val stateMsg = item?.stateMsg
            statusTv.text = when (item?.state?.toInt()) {
                1 -> {
                    layoutItem.shapeDrawableBuilder.setSolidColor(context.getColor(com.sjb.base.R.color.device_offline))
                        .intoBackground()
                   // statusTv.setTextColor(context.getColor(com.sjb.base.R.color.common_button_disable_color))
                    statusTv.setTextColor(context.getColor(com.sjb.base.R.color.black))
                    "离线"
                }

                2 -> {
                    layoutItem.shapeDrawableBuilder.setSolidColor(context.getColor(com.sjb.base.R.color.device_online))
                        .intoBackground()
                    statusTv.setTextColor(context.getColor(com.sjb.base.R.color.black))
                    "在线"
                }

                else -> {
                    statusTv.setTextColor(context.getColor(com.sjb.base.R.color.common_cancel_text_color))
                    "设备故障"
                }
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        // 返回一个 ViewHolder
        return VH(parent)
    }
}