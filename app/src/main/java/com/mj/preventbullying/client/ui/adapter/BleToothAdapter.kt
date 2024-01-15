package com.mj.preventbullying.client.ui.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.hjq.shape.view.ShapeButton
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.bletooth.BleTooth

/**
 * Create by MJ on 2024/1/15.
 * Describe :
 */

class BleToothAdapter : BaseQuickAdapter<BleTooth, QuickViewHolder>() {
    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: BleTooth?) {
        holder.getView<AppCompatTextView>(R.id.name_tv).text = item?.name
        holder.getView<AppCompatTextView>(R.id.mac_tv).text = item?.mac
        if (item?.connected == true) {
            holder.getView<ShapeButton>(R.id.ble_connect_bt).text = "已连接"
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_ble_tooth, parent)
    }


}