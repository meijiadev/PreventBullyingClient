package com.blackview.base.http

import androidx.lifecycle.viewModelScope

import com.mj.preventbullying.client.http.exception.AppException
import com.mj.preventbullying.client.http.exception.ExceptionHandle
import com.mj.preventbullying.client.http.request.BaseResponse
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel
import kotlinx.coroutines.*
import org.json.JSONObject
import retrofit2.HttpException

/**
 * 过滤服务器结果，失败直接抛异常，没有回到异常信息
 * @param block 请求体方法，必须要用suspend关键字修饰
 * @param success 成功回调
 * @param isShowDialog 是否显示加载框
 * @param loadingMessage 加载框提示内容
 */
fun <T> BaseViewModel.request(
    block: suspend () -> BaseResponse<T>,
    success: (T) -> Unit,
    isShowDialog: Boolean = true,
    loadingMessage: String = "请求网络中..."
): Job {
    //如果需要弹窗 通知Activity/fragment弹窗
    return viewModelScope.launch {
        runCatching {
            if (isShowDialog) uiChangeLiveData.showDialogEvent.postValue(loadingMessage)
            //请求体
            block()
        }.onSuccess {
            Logger.i("网络请求成功${it.toString()}")
            //网络请求成功 关闭弹窗
            uiChangeLiveData.dismissDialogEvent.postValue(null)
            runCatching {
                //校验请求结果码是否正确，不正确会抛出异常走下面的onFailure
                executeResponse(it) { t ->
                    success(t)
                }
            }.onFailure {
                //失败回调
                ExceptionHandle.handleException(it).message?.apply {
                    //打印错误消息
                    Logger.e(this)
                    uiChangeLiveData.toastEvent.postValue(this)
                }
            }
        }.onFailure {
            Logger.i("网络请求异常：${it.message}")
            //网络请求异常 关闭弹窗
            uiChangeLiveData.dismissDialogEvent.postValue(null)
            if (it is HttpException) {
                it.response()?.errorBody()?.string()?.apply {
                    if (this.isNotEmpty()) {
                        val message = JSONObject(this).optString("message")
                        uiChangeLiveData.toastEvent.postValue(message)
                        //token 过期 重新登录
                        if (message.contains("unauthenticated")) {
                            // SpManager.putString(USER_TOKEN,"")
                           // Constant.accessToken = null
                            //App.instance.gotoAct<LoginActivity>()
                        }

                    } else {
                        uiChangeLiveData.toastEvent.postValue(it.response()?.message())
                    }
                }
            } else {
                //失败回调
                ExceptionHandle.handleException(it).message?.apply {
                    //打印错误消息
                    Logger.e(this)
                    uiChangeLiveData.toastEvent.postValue(this)
                }
            }
        }
    }
}

/**
 * 过滤服务器结果，失败抛异常
 * @param block 请求体方法，必须要用suspend关键字修饰
 * @param success 成功回调
 * @param error 失败回调 可不传
 * @param isShowDialog 是否显示加载框
 * @param loadingMessage 加载框提示内容
 */
fun <T> BaseViewModel.request(
    block: suspend () -> BaseResponse<T>,
    success: (T) -> Unit,
    error: (AppException) -> Unit = {},
    isShowDialog: Boolean = true,
    loadingMessage: String = "请求网络中..."
): Job {
    //如果需要弹窗 通知Activity/fragment弹窗
    return viewModelScope.launch {
        runCatching {
            if (isShowDialog) uiChangeLiveData.showDialogEvent.postValue(loadingMessage)
            //请求体
            block()
        }.onSuccess {
            //网络请求成功 关闭弹窗
            uiChangeLiveData.dismissDialogEvent.postValue(null)
            runCatching {
                //校验请求结果码是否正确，不正确会抛出异常走下面的onFailure
                executeResponse(it) { t ->
                    success(t)
                }
            }.onFailure {
                error(ExceptionHandle.handleException(it))
                //打印错误消息
                ExceptionHandle.handleException(it).message?.apply {
                    //打印错误消息
                    Logger.e(this)
                    uiChangeLiveData.toastEvent.postValue(this)
                }
            }
        }.onFailure {
            //网络请求异常 关闭弹窗
            uiChangeLiveData.dismissDialogEvent.postValue(null)
            error(ExceptionHandle.handleException(it))
            if (it is HttpException) {
                it.response()?.errorBody()?.string()?.apply {
                    if (this.isNotEmpty()) {
                        val message = JSONObject(this).optString("message")
                        uiChangeLiveData.toastEvent.postValue(message)
                        //token 过期 重新登录
                        if (message.contains("unauthenticated")) {
                            // App.instance.gotoAct<LoginActivity>()
                            Logger.e("token 过期 重新登录")
                        }
                    } else {
                        uiChangeLiveData.toastEvent.postValue(it.response()?.message())
                    }
                }
            } else {
                //失败回调
                ExceptionHandle.handleException(it).message?.apply {
                    //打印错误消息
                    Logger.e(this)
                    uiChangeLiveData.toastEvent.postValue(this)
                }
            }
        }
    }
}

/**
 *  不过滤请求结果
 * @param block 请求体 必须要用suspend关键字修饰
 * @param success 成功回调
 * @param error 失败回调 可不给
 * @param isShowDialog 是否显示加载框
 * @param loadingMessage 加载框提示内容
 */
fun <T> BaseViewModel.requestNoCheck(
    block: suspend () -> T,
    success: (T) -> Unit,
    error: (AppException) -> Unit = {},
    isShowDialog: Boolean = true,
    loadingMessage: String = "请求网络中..."
): Job {
    //如果需要弹窗 通知Activity/fragment弹窗
    if (isShowDialog) uiChangeLiveData.showDialogEvent.postValue(loadingMessage)
    return viewModelScope.launch {
        runCatching {
            //请求体
            block()
        }.onSuccess {
            //网络请求成功 关闭弹窗
            uiChangeLiveData.dismissDialogEvent.postValue(null)
            //成功回调
            success(it)
        }.onFailure {
            Logger.e("${it.message}")
            //网络请求异常 关闭弹窗
            uiChangeLiveData.dismissDialogEvent.postValue(null)
            error(ExceptionHandle.handleException(it))
            if (it is HttpException) {
                it.response()?.errorBody()?.string()?.apply {
                    if (this.isNotEmpty()) {
                        val code = JSONObject(this).optString("code")
                        val message = JSONObject(this).optString("message")
                        uiChangeLiveData.toastEvent.postValue(message)
                        //token 过期 重新登录
                        if (message.contains("unauthenticated")) {
                            // App.instance.gotoAct<LoginActivity>()
                            Logger.e("token 过期重新登录")
                        }
                    } else {
                        uiChangeLiveData.toastEvent.postValue(it.response()?.message())
                    }
                }
            } else {
                //失败回调
                ExceptionHandle.handleException(it).message?.apply {
                    //打印错误消息
                    Logger.e(this)
                    uiChangeLiveData.toastEvent.postValue(this)
                }
            }
        }
    }
}

/**
 *  不过滤请求结果
 * @param block 请求体 必须要用suspend关键字修饰
 * @param success 成功回调
 * @param error 失败回调 可不给
 * @param isShowDialog 是否显示加载框
 * @param loadingMessage 加载框提示内容
 */
fun <T> BaseViewModel.requestNoCheckAndError(
    block: suspend () -> T,
    success: (T) -> Unit,
    error: (AppException) -> Unit = {},
    isShowDialog: Boolean = true,
    loadingMessage: String = "请求网络中..."
): Job {
    //如果需要弹窗 通知Activity/fragment弹窗
    if (isShowDialog) uiChangeLiveData.showDialogEvent.postValue(loadingMessage)
    return viewModelScope.launch {
        runCatching {
            //请求体
            block()
        }.onSuccess {
            //网络请求成功 关闭弹窗
            uiChangeLiveData.dismissDialogEvent.postValue(null)
            //成功回调
            success(it)
        }.onFailure {
            //网络请求异常 关闭弹窗
            uiChangeLiveData.dismissDialogEvent.postValue(null)
            if (it is HttpException) {
                it.response()?.errorBody()?.string()?.apply {
                    if (this.isNotEmpty()) {
                        val code = JSONObject(this).optInt("code")
                        val message = JSONObject(this).optString("message")
                        error(AppException(code, message))
                        uiChangeLiveData.toastEvent.postValue(message)
                        //token 过期 重新登录
                        if (message.contains("unauthenticated")) {
                            //  App.instance.gotoAct<LoginActivity>()
                            Logger.e("重新登录")
                        }
                    } else {
                        uiChangeLiveData.toastEvent.postValue(it.response()?.message())
                    }
                }
            } else {
                //失败回调
                ExceptionHandle.handleException(it).message?.apply {
                    //打印错误消息
                    Logger.e(this)
                    uiChangeLiveData.toastEvent.postValue(this)
                }
            }
        }
    }
}


/**
 * 请求结果过滤，判断请求服务器请求结果是否成功，不成功则会抛出异常
 */
suspend fun <T> executeResponse(
    response: BaseResponse<T>,
    success: suspend CoroutineScope.(T) -> Unit
) {
    coroutineScope {
        when (response.code) {
            0, 20000, 20004 -> {
                response.data?.let {
                    success(it)
                }
            }

            else -> {
                throw AppException(response.code, response.msg)
            }
        }
    }
}

/**
 *  调用协程
 * @param block 操作耗时操作任务
 * @param success 成功回调
 * @param error 失败回调 可不给
 */
fun <T> BaseViewModel.launch(
    block: () -> T,
    success: (T) -> Unit,
    error: (Throwable) -> Unit = {}
) {
    viewModelScope.launch {
        kotlin.runCatching {
            withContext(Dispatchers.IO) {
                block()
            }
        }.onSuccess {
            success(it)
        }.onFailure {
            error(it)
        }
    }
}
