package com.mj.preventbullying.client.foldtree

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.mj.preventbullying.client.R
import com.orhanobut.logger.Logger

/**
 * Create by MJ on 2023/12/21.
 * Describe :
 */

class TreeListAdapter : BaseQuickAdapter<TreeModel, QuickViewHolder>() {

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: TreeModel?) {
        val tvName = holder.getView<TextView>(R.id.tv_name)
        val ivNext = holder.getView<ImageView>(R.id.iv_next)
        // 设置数据
        tvName.text = item?.name
        if (item?.children != null && item.children!!.size > 0) {
            ivNext.visibility = View.VISIBLE
        } else {
            ivNext.visibility = View.GONE
        }
        if (item?.isOpen == true) {
            ivNext.rotation = 90f
        } else {
            ivNext.rotation = 0f
        }
        val left = item?.leave?.plus(1)?.times(30)
        // a+b => a.plus(b)
        // a*b => a.times(b)
        val layoutParams = tvName.layoutParams as LinearLayout.LayoutParams
        layoutParams.leftMargin = left ?: 30
        tvName.layoutParams = layoutParams
        tvName.tag = position


    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_tree, parent)
    }


    /**
     * 是展开还是关闭
     */
    fun <T : BaseModel<*>> setOpenOrClose(mList: MutableList<T>?, pos: Int) {
        mList?.let {
            val model = mList[pos]
            if (model.isOpen) {
                Logger.i("点击关闭")
                //如果是展开  把他关闭
                model.isOpen = false
                //移除子集
                removeChild(model.id, mList, 0)
            } else {
                Logger.i("点击展开")
                //关闭状态  就是展开
                model.isOpen=true
                val children = model.children as MutableList<T>
                val size = children.size
                //pos是你点击的item的position
                val leave: Int = model.getLeave() + 1
                for (i in 0 until size) {
                    children[i].setLeave(leave)
                }
                mList.addAll(pos + 1, children)
            }
        }
    }

    private fun <W : BaseModel<*>> removeChild(
        parentId: String,
        mList: MutableList<W>,
        start: Int
    ) {
        var removeIndex = start
        while (removeIndex < mList.size) {
            val model = mList[removeIndex]
            if (parentId == model.getParentId()) {
                mList.removeAt(removeIndex)
                removeIndex--
                //这里使用递归去删除子集的子集
                if (model.children != null && model.children.size > 0 && model.isOpen
                ) {
                    model.isOpen = false
                    removeChild<W>(model.id, mList, removeIndex)
                }
            }
            removeIndex++
        }
    }


}