package com.mj.preventbullying.client

/**
 * Create by MJ on 2023/12/4.
 * Describe :
 */

object Constant {

    const val USER_ID_KEY = "user_id_key"
    const val FRESH_TOKEN_KEY = "fresh_token_key"
    const val ACCESS_TOKEN_KEY = "access_token_key"
    const val REGISTER_ID_KEY = "register_id_key"
    const val EXPIRES_TIME_KEY = "expires_in_key"   // 过期时间
    const val LOGIN_OR_REFRESH_TIME_KEY = "last_login_or_fresh_time_key"  // 上次刷新或者登录时间
    const val ACCOUNT_KEY = "account_key"           // 用户名
    const val ACCOUNT_PASSWORD = "account_password"   // 密码
    const val USER_PHONE_KEY = "user_phone_key"               // 账号密码绑定的手机号码
    const val AUTO_LOGIN_KEY = "auto_login_key"    // 是否是自动登录 默认true

    var isNewAppVersion = false           // 是否有新版本app
    const val ALARM_PLAY_NAME_KEY = "alarm_audio_name_key"
    var alarmAudioName = "alarm_4.mp3"      // 默认的警报声音

    const val NOTIFY_MSG_EVENT_KEY = "notify_msg_event_key"  // 极光消息通知

    const val HTTP_RETURN_CODE_428 = 428
}


