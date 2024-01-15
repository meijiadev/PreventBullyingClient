package com.mj.preventbullying.client.bletooth

/**
 * Create by MJ on 2024/1/15.
 * Describe : 蓝牙设备的信息
 */

data class BleTooth(val name: String, val mac: String, var connected: Boolean = false)
