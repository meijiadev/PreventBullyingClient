package com.mj.preventbullying.client.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.tool.SpManager
import com.orhanobut.logger.Logger

/**
 * Create by MJ on 2024/1/3.
 * Describe : 提示音列表适配器
 */

class AlarmAudioAdapter : BaseQuickAdapter<String, QuickViewHolder>() {
    // 已选择的提示音
    private var alarmNameSelect: String? = null

    // 正在播放的提示音
    private var playingName: String? = null

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: String?) {
        //Logger.i("alarmName:$alarmNameSelect,item:$item")
        holder.getView<AppCompatTextView>(R.id.name_tv).text = item
        holder.getView<AppCompatImageView>(R.id.audio_select_iv).visibility =
            if (alarmNameSelect == item) {
                View.VISIBLE
            } else {
                View.GONE
            }
        val resourceId = if (playingName == item) {
            R.mipmap.play_status_icon
        } else {
            R.mipmap.pause_status_icon
        }
        holder.getView<AppCompatImageView>(R.id.play_iv).setImageResource(resourceId)

    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_alarm_audio, parent)
    }


    fun setSelectAlarm(alarm: String) {
        this.alarmNameSelect = alarm
        Logger.i("设置选中的audio:$alarm")
    }

    fun setPlayingName(name: String?) {
        this.playingName = name
    }

}