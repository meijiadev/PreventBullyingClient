package com.mj.preventbullying.client.ui

import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.sjb.base.base.BaseViewModel
import java.util.Timer
import java.util.TimerTask

/**
 * Create by MJ on 2023/12/12.
 * Describe :
 */

class TimerViewModel : BaseViewModel() {

    private lateinit var timer: Timer
    private var currentSecond = 0L

    private var timeEvent = UnPeekLiveData<String>()

    fun getCurrentTime(): UnPeekLiveData<String> {
        return timeEvent
    }

    fun startTimer() {
        timer = Timer()
        currentSecond = 0
        val timerTask = object : TimerTask() {
            override fun run() {
                currentSecond++
                timeEvent.postValue(getTime(currentSecond))
            }
        }
        timer.schedule(timerTask, 1000, 1000)
    }

    fun stopTimer(){
        timer.cancel()
    }

    /**
     * 把长整型转成时分秒
     */
    private fun getTime(time: Long): String {
        var sHour: String
        var sMinute: String
        var sSeconds: String
        val seconds = (time  % 60).toInt()
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
        timer.cancel()
    }
}