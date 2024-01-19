package com.mj.preventbullying.client.ui.viewmodel

import cn.jpush.android.api.NotificationMessage
import com.blackview.base.http.requestNoCheck
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.foldtree.TreeModel
import com.mj.preventbullying.client.http.apiService
import com.mj.preventbullying.client.http.result.UpdateAppResult
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2023/12/27.
 * Describe : 推送消息
 */

class GlobalEventViewModel : BaseViewModel() {

    /**
     * http请求返回code值
     */
    var codeEvent = UnPeekLiveData<Int>()

    var notifyMsgEvent = UnPeekLiveData<NotificationMessage>()

    /**
     * App更新事件
     */
    var updateAppEvent = UnPeekLiveData<UpdateAppResult>()

    var treeList: MutableList<TreeModel>? = null

    var orgTreeEvent = UnPeekLiveData<MutableList<TreeModel>>()

    /**
     * 修改组织Id
     */
    var orgEvent = UnPeekLiveData<String>()


    private var schoolName: String? = null
    private var schoolId: String? = null

    fun getAppVersion() {
        requestNoCheck({
            apiService.getNewApp()
        }, {
            //   Logger.i("获取的最新app版本信息：$it")
            MyApp.globalEventViewModel.updateAppEvent.postValue(it)
        })
    }


    /**
     * 获取组织列表
     */
    fun getOrgList() {
        requestNoCheck({
            apiService.getOrgTree()
        }, { it ->
            // 接收到组织树列表
            val tree = it.data
            val gson = Gson()
            val jsonStr = gson.toJson(tree)
            treeList = gson.fromJson(jsonStr, object : TypeToken<List<TreeModel?>?>() {}.type)
            Logger.d("转化之后的组织树：${treeList?.size}")
            treeList?.let { it1 -> findFirstSchool(it1) }
            orgTreeEvent.postValue(treeList)
        })
    }

    private fun findFirstSchool(list: MutableList<TreeModel>) {
        val tree1 = list.find { it.type == "1" }
        val tree0 = list.find { it.type == "0" }
        if (tree1 != null) {
            findFirstSchool(tree1.children)
        } else {
            schoolName = tree0?.name
            schoolId = tree0?.id
        }
    }

    /**
     * 获取组织名称
     */
    fun getSchoolName(): String? {
        return schoolName
    }

    fun setSchoolName(name: String) {
        this.schoolName = name
    }

    /**
     * 获取组织Id
     */
    fun getSchoolId(): String? {
        return schoolId
    }

    fun setSchoolId(id: String) {
        this.schoolId = id
        orgEvent.postValue(id)
    }


}