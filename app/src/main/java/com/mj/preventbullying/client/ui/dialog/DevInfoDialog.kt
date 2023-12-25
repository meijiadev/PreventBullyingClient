package com.mj.preventbullying.client.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hjq.shape.layout.ShapeLinearLayout
import com.hjq.shape.layout.ShapeRelativeLayout
import com.hjq.shape.view.ShapeEditText
import com.hjq.shape.view.ShapeTextView
import com.hjq.toast.ToastUtils
import com.lxj.xpopup.core.CenterPopupView
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.foldtree.TreeListAdapter
import com.mj.preventbullying.client.foldtree.TreeModel
import com.mj.preventbullying.client.http.result.DevType
import com.mj.preventbullying.client.http.result.DeviceRecord
import com.mj.preventbullying.client.http.result.RecordData
import com.mj.preventbullying.client.ui.adapter.DevTypeAdapter
import com.orhanobut.logger.Logger

/**
 * Create by MJ on 2023/12/21.
 * Describe : 添加设备的弹窗
 */

class DevInfoDialog(context: Context) : CenterPopupView(context) {

    private val addDevLayout: ShapeRelativeLayout by lazy { findViewById(R.id.add_dev_layout) }
    private val titleTv: AppCompatTextView by lazy { findViewById(R.id.title_tv) }
    private val snLl: ShapeLinearLayout by lazy { findViewById(R.id.sn_ll) }
    private val nameEt: ShapeEditText by lazy { findViewById(R.id.name_et) }
    private val snEt: EditText by lazy { findViewById(R.id.sn_et) }
    private val devTypeTv: ShapeTextView by lazy { findViewById(R.id.dev_type_tv) }
    private val orgListTv: ShapeTextView by lazy { findViewById(R.id.org_list_tv) }
    private val locationEt: ShapeEditText by lazy { findViewById(R.id.location_et) }
    private val cancelTv: ShapeTextView by lazy { findViewById(R.id.cancel_tv) }
    private val confirmTv: ShapeTextView by lazy { findViewById(R.id.confirm_tv) }
    private val orgListLl: ShapeLinearLayout by lazy { findViewById(R.id.org_list_ll) }
    private val orgEnterIv: AppCompatImageView by lazy { findViewById(R.id.org_enter_iv) }
    private val typeEnterIv: AppCompatImageView by lazy { findViewById(R.id.type_enter_iv) }
    private val devTypeLl: ShapeLinearLayout by lazy { findViewById(R.id.dev_type_ll) }
    private val orgLayoutLl: ShapeLinearLayout by lazy { findViewById(R.id.org_ll) }
    private val desEt: ShapeEditText by lazy { findViewById(R.id.des_et) }


    private var context: Context

    private var listener: AddDevListener? = null
    private val itemListRecycler: RecyclerView by lazy { findViewById(R.id.list_item_recycler) }
    private var treeAdapter: TreeListAdapter? = null
    private var orgData: MutableList<TreeModel>? = null
    private var typeData: MutableList<DevType>? = null
    private var devTypeAdapter: DevTypeAdapter? = null
    private var curOrgId: Long = 0
    private var titleMsg: String? = null

    private var devData: DeviceRecord? = null

    init {
        this.context = context
    }

    override fun getImplLayoutId(): Int = R.layout.dialog_add_device


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate() {
        super.onCreate()
        titleTv.text = titleMsg
        if (titleMsg == "修改设备信息") {
            Logger.i("将sn码编辑框设置成不可点击")
            snEt.isEnabled = false
            snLl.isClickable = false
            snLl.shapeDrawableBuilder.setSolidColor(context.getColor(com.sjb.base.R.color.common_line_color))
                .intoBackground()
        }
        devData?.let {
            snEt.setText(it.snCode)
            nameEt.setText(it.name)
            curOrgId = it.org.id.toLong()
            orgListTv.text = it.org.name
            devTypeTv.text = it.modelCode
            locationEt.setText(it.location)
            desEt.setText(it.description)
        }
        treeAdapter = TreeListAdapter()
        devTypeAdapter = DevTypeAdapter()
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        itemListRecycler.layoutManager = layoutManager

        // 组织选择
        orgLayoutLl.setOnClickListener {
            if (orgListLl.visibility == View.GONE) {
                refreshOrgList()
                val layoutParams = orgListLl.layoutParams as RelativeLayout.LayoutParams
                layoutParams.addRule(RelativeLayout.BELOW, R.id.org_ll)
                orgListLl.layoutParams = layoutParams
                orgEnterIv.rotation = 90f
                orgListLl.visibility = View.VISIBLE
            } else {
                orgEnterIv.rotation = 0f
                orgListLl.visibility = View.GONE
            }
        }
        // 设备型号选择
        devTypeLl.setOnClickListener {
            if (orgListLl.visibility == View.GONE) {
                refreshDevList()
                val layoutParams = orgListLl.layoutParams as RelativeLayout.LayoutParams
                layoutParams.addRule(RelativeLayout.BELOW, R.id.dev_type_ll)
                orgListLl.layoutParams = layoutParams

                typeEnterIv.rotation = 90f
                orgListLl.visibility = View.VISIBLE
            } else {
                typeEnterIv.rotation = 0f
                orgListLl.visibility = View.GONE
            }
        }


        treeAdapter?.setOnItemClickListener { adapter, view, position ->
            val mode = orgData?.get(position)
            if (mode?.children != null) {
                treeAdapter?.setOpenOrClose(orgData, position)
                treeAdapter?.notifyDataSetChanged()
            } else {
                val org = orgData?.get(position)
                Logger.i("点击的项：${org.toString()}")
                orgListTv.text = org?.name

                runCatching {
                    curOrgId = org?.id?.toLong() ?: 0
                }.onFailure {
                    Logger.e("error:${it.message}")
                    curOrgId = 0
                }
                orgEnterIv.rotation = 0f
                orgListLl.visibility = View.GONE

            }
        }

        devTypeAdapter?.setOnItemClickListener { adapter, v, position ->
            val type = typeData?.get(position)
            Logger.i("点击的项：${type}")
            devTypeTv.text = type?.value
            orgEnterIv.rotation = 0f
            orgListLl.visibility = View.GONE
        }

        confirmTv.setOnClickListener {
            val sn = snEt.text.toString()
            val name = nameEt.text.toString()
            val orgMsg = orgListTv.text.toString()
            val devType = devTypeTv.text.toString()
            val location = locationEt.text.toString()
            val desc = desEt.text.toString()
            if (sn.isEmpty() || name.isEmpty() || orgMsg.isEmpty() || devType.isEmpty() || location.isEmpty()) {
                ToastUtils.show("请按照指示填写必填信息")
            } else {
                listener?.onConfirm(sn, name, curOrgId,orgMsg, location, devType, desc)
                dismiss()
            }
        }

        cancelTv.setOnClickListener {
            dismiss()
        }

    }


    /**
     *  刷新组织列表
     */
    @SuppressLint("NotifyDataSetChanged")
    fun refreshOrgList() {
//        treeAdapter.setOpenOrClose()
        itemListRecycler.adapter = treeAdapter
        treeAdapter?.submitList(orgData)
        treeAdapter?.notifyDataSetChanged()
    }

    /**
     * 刷新设备型号列表
     */
    @SuppressLint("NotifyDataSetChanged")
    fun refreshDevList() {
        itemListRecycler.adapter = devTypeAdapter
        devTypeAdapter?.submitList(typeData)
        devTypeAdapter?.notifyDataSetChanged()
    }


    fun setOnListener(listener: AddDevListener): DevInfoDialog = apply {
        this.listener = listener
    }

    fun setOrgData(data: MutableList<TreeModel>?): DevInfoDialog = apply {
       // Logger.i("列表：${data?.size}")
        this.orgData = data
    }

    fun setTypeData(data: MutableList<DevType>?): DevInfoDialog = apply {
        this.typeData = data
    }

    fun setTitleMsg(msg: String): DevInfoDialog = apply {
        this.titleMsg = msg
    }

    /**
     * 设置修改的数据
     */
    fun setAmendData(deviceRecord: DeviceRecord?): DevInfoDialog = apply {
        this.devData = deviceRecord
        Logger.i("需要修改的数据：$deviceRecord")
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
               // Logger.i("点击事件:按下")
            }

            MotionEvent.ACTION_UP -> {
                //Logger.i("点击事件:抬起")
                orgListLl.visibility = View.GONE
            }
        }

        return super.onTouchEvent(event)
    }

    interface AddDevListener {
        fun onCancel()
        fun onConfirm(
            sn: String,
            name: String,
            orgId: Long,
            orgName:String,
            location: String,
            modelCode: String,
            desc: String?
        )
    }
}