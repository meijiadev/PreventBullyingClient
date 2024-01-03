package com.sjb.base.base

import androidx.lifecycle.ViewModel
import com.kunminx.architecture.ui.callback.UnPeekLiveData

/**
 * Create by MJ on 2023/12/4.
 * Describe :
 */

open class BaseViewModel : ViewModel() {
    val uiChangeLiveData = UIChangeLiveData()

    companion object {
        class UIChangeLiveData : UnPeekLiveData<Void>() {
            val showDialogEvent = UnPeekLiveData<String>()
            val dismissDialogEvent = UnPeekLiveData<Void>()
            val finishEvent = UnPeekLiveData<Void>()
            val uiMessageEvent = UnPeekLiveData<UIMessage>()
            val toastEvent = UnPeekLiveData<String>()
            val alarmEvent = UnPeekLiveData<Boolean>()
        }
    }
}