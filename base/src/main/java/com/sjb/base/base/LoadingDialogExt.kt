package com.sjb.base.base

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import com.orhanobut.logger.Logger
import com.sjb.base.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 *    author : Mj
 *    time   : 2022.04.13
 *    desc   : 等待加载对话框
 */

/** 加载对话框 */
private var loadingPopup: LoadingPopupView? = null
/**
 * 打开等待框
 */
fun AppCompatActivity.showLoadingExt(message: Int = R.string.common_loading) {
    if (isFinishing || isDestroyed) {
        return
    }
    MainScope().launch(Dispatchers.Main){
        if (isFinishing || isDestroyed) {
            return@launch
        }
        if (loadingPopup == null) {
            loadingPopup =XPopup.Builder(this@showLoadingExt)
                .dismissOnBackPressed(false)
                .hasShadowBg(false) // 去掉半透明背景
                .hasBlurBg(true)
                .isViewMode(true)
                .asLoading(getString(message)) as LoadingPopupView
        }
        if (false==loadingPopup?.isShow) {
            kotlin.runCatching {
                loadingPopup?.show()
                Logger.i("打开等待框")
            }.onFailure {
                Logger.e(it.toString())
                //CrashReport.postCatchedException(it)
            }
        }
    }

}

/**
 * 打开等待框
 */
fun Fragment.showLoadingExt(message:Int = R.string.common_loading) {
    activity?.let {
        if (it.isFinishing || it.isDestroyed) {
            return
        }
        MainScope().launch(Dispatchers.Main){
            if (it.isFinishing || it.isDestroyed) {
                return@launch
            }
            if (loadingPopup == null) {
                loadingPopup =XPopup.Builder(it)
                    .dismissOnBackPressed(true)
                    .dismissOnTouchOutside(false)
                    .hasShadowBg(false) // 去掉半透明背景
                    .isViewMode(true)
                    .asLoading(getString(message)) as LoadingPopupView
            }
            if (false== loadingPopup?.isShow&&!it.isFinishing&&!it.isDestroyed) {
                loadingPopup?.show()
                Logger.i("打开等待框")
            }
        }
    }
}

fun isShowDialogExt(): Boolean {
    return loadingPopup != null && true== loadingPopup?.isShow
}



/**
 * 关闭等待框
 */
fun dismissLoadingExt() {
    MainScope().launch (Dispatchers.Main){
        if ((loadingPopup == null) || false== loadingPopup?.isShow) {
            return@launch
        }
        loadingPopup?.delayDismiss(300)
        loadingPopup=null
        Logger.i("关闭等待框")
    }
}



