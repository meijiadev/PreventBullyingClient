package com.mj.preventbullying.client.http.request


import com.mj.preventbullying.client.http.exception.AppException


open class BaseResponse<T> {
    var message: String? = null
    var code = 0
    var data: T? = null

    override fun toString(): String {
        return "$message:$message\n" +
                "code:$code\n" +
                "data:$data"

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