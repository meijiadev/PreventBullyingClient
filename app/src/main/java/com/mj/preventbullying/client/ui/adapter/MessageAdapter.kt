package com.mj.preventbullying.client.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.mj.preventbullying.client.http.result.Record
import com.mj.preventbullying.client.databinding.ItemMessageRecordNewBinding

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */
const val PENDING_STATUS = "0"           //待处理
const val PROCESSING_STATUS = "1"        // 处理中
const val PROCESSED_STATUS = "2"         // 已处理
const val PROCESSED_IGNORE = "3"         // 已忽略

class MessageAdapter : BaseQuickAdapter<Record, MessageAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: ItemMessageRecordNewBinding = ItemMessageRecordNewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: Record?) {
        //设置item数据
        holder.binding.apply {
            snTv.text = "SN-${item?.snCode}"
            locationTv.text = "${item?.org?.name} ${item?.location}"
            keywordTv.text = item?.keyword
            volumeTv.text = "${item?.volume}|${item?.credibility}"
            waringTimeTv.text = item?.waringTime
            when (item?.state) {
                // 待处理
                PENDING_STATUS -> {
                    processTv.text = "待处理"
                    processTv.shapeDrawableBuilder.setSolidColor(context.getColor(com.sjb.base.R.color.red))
                        .intoBackground()
                    processBt.visibility = View.VISIBLE
                    callTv.visibility=View.VISIBLE
                }
                // 处理中
                PROCESSING_STATUS -> {
                    processTv.text = "处理中"
                    processTv.shapeDrawableBuilder.setSolidColor(context.getColor(com.sjb.base.R.color.gold))
                        .intoBackground()
                    processBt.visibility = View.VISIBLE
                    callTv.visibility=View.VISIBLE
                }
                // 已处理
                PROCESSED_STATUS -> {
                    processTv.text = "已处理"
                    processTv.shapeDrawableBuilder.setSolidColor(context.getColor(com.sjb.base.R.color.green))
                        .intoBackground()
                    processBt.visibility = View.GONE
                    callTv.visibility=View.GONE
                }
                // 已忽略
                PROCESSED_IGNORE -> {
                    processTv.text = "已忽略"
                    processTv.shapeDrawableBuilder.setSolidColor(context.getColor(com.sjb.base.R.color.gray))
                        .intoBackground()
                    processBt.visibility = View.GONE
                    callTv.visibility=View.GONE
                }

                else -> null
            }

        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

}