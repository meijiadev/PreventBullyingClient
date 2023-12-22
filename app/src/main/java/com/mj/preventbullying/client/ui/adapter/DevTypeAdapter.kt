package com.mj.preventbullying.client.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.http.result.DevType

/**
 * Create by MJ on 2023/12/22.
 * Describe :
 */

class DevTypeAdapter : BaseQuickAdapter<DevType, QuickViewHolder>() {
    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: DevType?) {
        holder.getView<AppCompatTextView>(R.id.dev_type_tv).text=item?.value
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_dev_type, parent)
    }

}