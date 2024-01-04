package com.mj.preventbullying.client.tool

import android.R
import android.app.Notification
import androidx.appcompat.app.AppCompatActivity
import cn.jpush.android.api.BasicPushNotificationBuilder
import cn.jpush.android.api.JPushInterface
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.orhanobut.logger.Logger


/**
 * Create by MJ on 2024/1/3.
 * Describe :
 */

fun AppCompatActivity.requestPermission() {
    XXPermissions.with(this)
        // .permission(Permission.MANAGE_EXTERNAL_STORAGE)
        .permission(Permission.RECORD_AUDIO)
        .permission(Permission.ACCESS_FINE_LOCATION)
        .permission(Permission.ACCESS_COARSE_LOCATION)
        .request(object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                // Logger.i("录音权限获取成功")
                if (all) {
                    Logger.i("所有权限获取成功")
                } else {
                    permissions?.let {
                        for (permission in it) {
                            Logger.i("获取到的权限：$permission")
                        }
                    }

                }

            }

            override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                super.onDenied(permissions, never)
                //Logger.i("权限获取失败")
                permissions?.let {
                    for (permission in it) {
                        Logger.i("权限获取失败：$permission")
                    }
                }
            }
        })

}

fun AppCompatActivity.getAssetsList(): List<String>? {
    val fileNames = assets.list("")
    val alarms = mutableListOf<String>()
    return if (fileNames != null) {
        for (name in fileNames) {
            if (name.contains(".mp3")) {
                alarms.add(name)
            }
        }
        alarms
    } else {
        null
    }

}

/**
 * 获取后台定位权限
 */
fun AppCompatActivity.requestLocationPermission() {
    XXPermissions.with(this)
        // .permission(Permission.MANAGE_EXTERNAL_STORAGE)
        .permission(Permission.ACCESS_BACKGROUND_LOCATION)
        .request(object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                // Logger.i("录音权限获取成功")
                if (all) {
                    Logger.i("所有权限获取成功")
                } else {
                    permissions?.let {
                        for (permission in it) {
                            Logger.i("获取到的权限：$permission")
                        }
                    }

                }

            }

            override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                super.onDenied(permissions, never)
                //Logger.i("权限获取失败")
                permissions?.let {
                    for (permission in it) {
                        Logger.i("权限获取失败：$permission")
                    }
                }
            }
        })
}
