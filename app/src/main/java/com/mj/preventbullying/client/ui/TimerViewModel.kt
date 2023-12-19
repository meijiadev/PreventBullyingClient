package com.mj.preventbullying.client.ui

import androidx.lifecycle.viewModelScope
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

/**
 * Create by MJ on 2023/12/12.
 * Describe :
 */

class TimerViewModel : BaseViewModel() {


    private var currentSecond = 0L

    private var timeEvent = UnPeekLiveData<String>()
    var isRunning = false

    fun getCurrentTime(): UnPeekLiveData<String> {
        return timeEvent
    }

    fun startTimer() {
        isRunning = true
        currentSecond = 0
        viewModelScope.launch(Dispatchers.IO) {
            while (isRunning) {
                delay(1000)
                currentSecond++
                timeEvent.postValue(getTime(currentSecond))
            }
        }
    }

    fun stopTimer() {
        currentSecond = 0
        isRunning = false
    }


    /**
     * 把长整型转成时分秒
     */
    private fun getTime(time: Long): String {
        var sHour: String
        var sMinute: String
        var sSeconds: String
        val seconds = (time % 60).toInt()
        val minutes = ((time / 60) % 60).toInt()
        val hour = ((time / (60 * 60)) % 60).toInt()
        sHour = if (hour < 10) {
            "0$hour"
        } else {
            hour.toString()
        }
        sMinute = if (minutes < 10) {
            "0$minutes"
        } else {
            minutes.toString()
        }
        sSeconds = if (seconds < 10) {
            "0$seconds"
        } else {
            seconds.toString()
        }
        return "$sHour:$sMinute:$sSeconds"
    }


    override fun onCleared() {
        super.onCleared()
        isRunning = false
    }
}