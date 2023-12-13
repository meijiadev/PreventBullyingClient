package com.mj.preventbullying.client.http.request


import com.mj.preventbullying.client.http.exception.AppException


open class BaseResponse<T> {
    var msg: String? = null
    var code = 0
    var data: T? = null
    var success = false

    override fun toString(): String {
        return "$msg:$msg\n" +
                "code:$code\n" +
                "data:$data\n" +
                "success:$success"

    }
}

data class BaseResponseNotData(
    val message: String,
    val code: Int
)

class StartResponse<T> : BaseResponse<T>()

data class SuccessResponse<T>(var data1: T) : BaseResponse<T>()

class EmptyResponse<T> : BaseResponse<T>()

data class FailureResponse<T>(val exception: AppException) : BaseResponse<T>()

data class Region(val id: String, val name: String)