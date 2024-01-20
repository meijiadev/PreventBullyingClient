package com.mj.preventbullying.client.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.mj.preventbullying.client.databinding.ItemListCommonBinding
import com.mj.preventbullying.client.http.result.VRecord
import retrofit2.http.PATCH

/**
 * Create by MJ on 2024/1/20.
 * Describe :
 */

class VoiceAdapter : BaseQuickAdapter<VRecord, VoiceAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: ItemListCommonBinding = ItemListCommonBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: VRecord?) {
        holder.binding.commonNameTv.text = item?.text
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}