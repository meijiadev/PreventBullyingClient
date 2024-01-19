package com.mj.preventbullying.client.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.mj.preventbullying.client.databinding.ItemDeviceNewListBinding
import com.mj.preventbullying.client.databinding.ItemKeywordListBinding
import com.mj.preventbullying.client.http.result.KRecord

/**
 * Create by MJ on 2024/1/19.
 * Describe :
 */

class KeywordAdapter : BaseQuickAdapter<KRecord, KeywordAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: ItemKeywordListBinding = ItemKeywordListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: KRecord?) {
        holder.binding.apply {
            keywordTv.text = item?.keyword
            interventionTv.text = "语音文案：\n" + item?.voice?.name
            gradeTv.text = "等级：" + when (item?.level) {
                1 -> "严重"
                2 -> "一般"
                3 -> "轻微"
                else -> ""
            }
            enableBt.setChecked(item?.enabled ?: false)
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}