package com.mj.preventbullying.client.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lxj.xpopup.core.PositionPopupView
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.foldtree.TreeListAdapter
import com.mj.preventbullying.client.foldtree.TreeModel
import com.mj.preventbullying.client.http.result.DevType
import com.mj.preventbullying.client.http.result.Org
import com.mj.preventbullying.client.ui.adapter.DevTypeAdapter
import com.orhanobut.logger.Logger

/**
 * Create by MJ on 2023/12/21.
 * Describe :
 */

class ItemListDialog(context: Context) : PositionPopupView(context) {
    private val itemListRecycler: RecyclerView by lazy { findViewById(R.id.list_item_recycler) }

    // 设备注册相关页面
    private var orgList: MutableList<TreeModel>? = null
    private var typeList: MutableList<DevType>? = null
    private var treeAdapter: TreeListAdapter? = null
    private var devTypeAdapter: DevTypeAdapter? = null

    private var curOrgId: Long = 0
    override fun getImplLayoutId(): Int = R.layout.dialog_list

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate() {
        super.onCreate()
        treeAdapter = TreeListAdapter()
        devTypeAdapter = DevTypeAdapter()
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        itemListRecycler.layoutManager = layoutManager
        Logger.i("初始化")
        if (orgList != null) {
            itemListRecycler.adapter = treeAdapter
            treeAdapter?.submitList(orgList)
        }
        if (typeList != null) {
            itemListRecycler.adapter = devTypeAdapter
            devTypeAdapter?.submitList(typeList)
        }
        treeAdapter?.setOnItemClickListener { adapter, view, position ->
            val mode = orgList?.get(position)
            if (mode?.children != null) {
                treeAdapter?.setOpenOrClose(orgList, position)
                treeAdapter?.notifyDataSetChanged()
            } else {
                val org = orgList?.get(position)
                Logger.i("点击的项：${org.toString()}")
                //orgListTv.text = org?.name

                runCatching {
                    curOrgId = org?.id?.toLong() ?: 0
                }.onFailure {
                    Logger.e("error:${it.message}")
                    curOrgId = 0
                }
                org?.let {
                    orgSelectListener?.invoke(Org(org.id, org.name))
                    dismiss()
                }

            }
        }

        devTypeAdapter?.setOnItemClickListener { adapter, v, position ->
            val type = typeList?.get(position)
            type?.value?.let {
                devTypeListener?.invoke(it)
                dismiss()
            }
            Logger.i("点击的项：${type}")
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setOrgData(data: MutableList<TreeModel>?): ItemListDialog = apply {
        Logger.i("列表：${data?.size}")
        treeAdapter?.submitList(data)
        treeAdapter?.notifyDataSetChanged()
        typeList = null
        this.orgList = data
    }

    /**
     * 设置设备类型的列表
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setDevType(types: MutableList<DevType>?): ItemListDialog = apply {
        devTypeAdapter?.submitList(types)
        devTypeAdapter?.notifyDataSetChanged()
        orgList = null
        this.typeList = types
    }

    private var orgSelectListener: ((org: Org) -> Unit)? = null
    private var devTypeListener: ((type: String) -> Unit)? = null

    /**
     * 监听组织列表
     */
    fun onOrgListener(listener: ((org: Org) -> Unit)): ItemListDialog = apply {
        this.orgSelectListener = listener
    }

    /**
     * 监听设备类型
     */
    fun onTypeListener(listener: ((type: String) -> Unit)): ItemListDialog = apply {
        this.devTypeListener = listener
    }
}