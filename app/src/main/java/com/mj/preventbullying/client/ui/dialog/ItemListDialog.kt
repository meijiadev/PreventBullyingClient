package com.mj.preventbullying.client.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lxj.xpopup.core.PositionPopupView
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.foldtree.TreeListAdapter
import com.mj.preventbullying.client.foldtree.TreeModel
import com.orhanobut.logger.Logger

/**
 * Create by MJ on 2023/12/21.
 * Describe :
 */

class ItemListDialog(context: Context) : PositionPopupView(context) {
    private val itemListRecycler: RecyclerView by lazy { findViewById(R.id.list_item_recycler) }
    private var treeAdapter: TreeListAdapter? = null
    private var data: MutableList<TreeModel>? = null
    override fun getImplLayoutId(): Int = R.layout.dialog_item_list

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate() {
        super.onCreate()
        treeAdapter = TreeListAdapter()
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        itemListRecycler.layoutManager = layoutManager
        itemListRecycler.adapter = treeAdapter

        treeAdapter?.setOnItemClickListener { adapter, view, position ->
            val mode = data?.get(position)
            if (mode?.children != null) {
                treeAdapter?.setOpenOrClose(data, position)
                treeAdapter?.notifyDataSetChanged()
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: MutableList<TreeModel>?): ItemListDialog = apply {
        Logger.i("列表：${data?.size}")
        treeAdapter?.submitList(data)
        treeAdapter?.notifyDataSetChanged()
        this.data = data
    }
}