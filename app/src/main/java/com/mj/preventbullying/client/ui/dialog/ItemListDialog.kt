package com.mj.preventbullying.client.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lxj.xpopup.core.PositionPopupView
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.foldtree.TreeListAdapter
import com.mj.preventbullying.client.foldtree.TreeModel
import com.mj.preventbullying.client.http.result.DevType
import com.mj.preventbullying.client.http.result.Org
import com.mj.preventbullying.client.http.result.VRecord
import com.mj.preventbullying.client.ui.adapter.DevTypeAdapter
import com.mj.preventbullying.client.ui.adapter.VoiceAdapter
import com.mj.preventbullying.client.ui.view.MaxHeightRecyclerView
import com.orhanobut.logger.Logger

/**
 * Create by MJ on 2023/12/21.
 * Describe :
 */

class ItemListDialog(context: Context) : PositionPopupView(context) {
    private val itemListRecycler: MaxHeightRecyclerView by lazy { findViewById(R.id.list_item_recycler) }
    private val addIv: AppCompatImageView by lazy { findViewById(R.id.add_iv) }

    // 设备注册相关页面
    private var orgList: MutableList<TreeModel>? = null
    private var typeList: MutableList<DevType>? = null
    private var voiceList: List<VRecord>? = null

    private var treeAdapter: TreeListAdapter? = null
    private var devTypeAdapter: DevTypeAdapter? = null
    private var voiceAdapter: VoiceAdapter? = null

    private var curOrgId: Long = 0
    override fun getImplLayoutId(): Int = R.layout.dialog_list

    override fun onCreate() {
        super.onCreate()
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        itemListRecycler.layoutManager = layoutManager
        Logger.i("初始化")

        initOrgList()
        initTypeList()
        initVoiceLayout()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initOrgList() {
        if (orgList != null) {
            treeAdapter = TreeListAdapter()
            itemListRecycler.adapter = treeAdapter
            treeAdapter?.submitList(orgList)
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

        }
    }

    /**
     * 初始化设备类型
     */
    private fun initTypeList() {
        if (typeList != null) {
            devTypeAdapter = DevTypeAdapter()
            itemListRecycler.adapter = devTypeAdapter
            devTypeAdapter?.submitList(typeList)
            devTypeAdapter?.setOnItemClickListener { adapter, v, position ->
                val type = typeList?.get(position)
                type?.value?.let {
                    devTypeListener?.invoke(it)
                    dismiss()
                }
                Logger.i("点击的项：${type}")
            }

        }

    }

    private fun initVoiceLayout() {
        if (voiceList != null) {
            voiceAdapter = VoiceAdapter()
            itemListRecycler.adapter = voiceAdapter
            voiceAdapter?.submitList(voiceList)
            voiceAdapter?.setOnItemClickListener { adapter, v, position ->
                voiceList?.get(position)?.let {
                    voiceListener?.invoke(it)
                    dismiss()
                }
            }
            addIv.visibility = VISIBLE
            addIv.setOnClickListener {
                addVoiceListener?.invoke()
                dismiss()
            }
        } else {
            addIv.visibility = GONE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setOrgData(data: MutableList<TreeModel>?): ItemListDialog = apply {
        Logger.i("列表：${data?.size}")
        treeAdapter?.submitList(data)
        treeAdapter?.notifyDataSetChanged()
        typeList = null
        voiceList = null
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
        voiceList = null
        this.typeList = types
    }

    /**
     * 设置语音播报的列表
     */
    fun setVoiceList(voices: List<VRecord>?): ItemListDialog = apply {
        orgList = null
        typeList = null
        this.voiceList = voices
    }


    private var orgSelectListener: ((org: Org) -> Unit)? = null
    private var devTypeListener: ((type: String) -> Unit)? = null

    private var voiceListener: ((v: VRecord) -> Unit)? = null

    private var addVoiceListener: (() -> Unit)? = null

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

    fun onVoiceListener(listener: ((v: VRecord) -> Unit)): ItemListDialog = apply {
        this.voiceListener = listener
    }

    fun onAddVListener(listener: () -> Unit): ItemListDialog = apply {
        this.addVoiceListener = listener
    }
}