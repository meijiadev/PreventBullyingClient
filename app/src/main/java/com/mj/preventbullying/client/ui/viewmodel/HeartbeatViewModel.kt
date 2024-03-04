package com.mj.preventbullying.client.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.blackview.base.http.requestNoCheck
import com.mj.preventbullying.client.http.apiService
import com.sjb.base.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.wait

/**
 * Create by MJ on 2024/3/4.
 * Describe :
 */

class HeartbeatViewModel : BaseViewModel() {
    private var isHearting = false
    private var job: Job? = null

    fun stopSend() {
        isHearting = false
        job?.cancel()
    }

    fun sendHeartbeat(sn: String) {
        isHearting = true
        job = viewModelScope.launch {
            while (isHearting) {
                requestNoCheck({
                    apiService.streamHeartbeat(sn)
                }, {

                })
                delay(3 * 1000)
            }
        }
    }
}