//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.mj.preventbullying.client.http;

import android.text.TextUtils;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hjq.toast.ToastUtils;
import com.mj.preventbullying.client.MyApp;
import com.mj.preventbullying.client.http.request.BaseResponse;
import com.mj.preventbullying.client.ui.login.LoginActivity;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Response.Builder;
import okhttp3.ResponseBody;
import okio.Buffer;


/**
 * 自定义的一个日志过滤器
 *
 * @author zengyi
 * create at 2016/9/19 16:08
 */

public class LoggerInterceptor implements Interceptor {

    private String tag = "Http";
    private boolean showResponse = true;

    public LoggerInterceptor(String tag, boolean showResponse) {
        this.showResponse = showResponse;
        this.tag = tag;
    }

    public LoggerInterceptor() {

    }

    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        this.logForRequest(request);
        Response response = chain.proceed(request);
        return this.logForResponse(response);
    }

    private Response logForResponse(Response response) {
        try {
            Logger.d("============================response\'log============================");
            Builder e = response.newBuilder();
            Response clone = e.build();
            Logger.d("url : " + clone.request().url());
            Logger.d("code : " + clone.code());
            Logger.d("protocol : " + clone.protocol());
            if (!TextUtils.isEmpty(clone.message())) {
                Logger.d("message : " + clone.message());
            }
            if (this.showResponse) {
                ResponseBody body = clone.body();
                if (body != null) {
                    MediaType mediaType = body.contentType();
                    if (mediaType != null) {
                        Logger.d("responseBody\'s contentType : " + mediaType);
                        if (this.isText(mediaType)) {
                            String resp = body.string();
                            Logger.d("responseBody\'s content : " + resp);
                            Map<String, Object> jsonMap = new Gson().fromJson(resp, new TypeToken<Map<String, Object>>() {
                            }.getType());
                            Object codeObject = jsonMap.get("code");
                            if (codeObject != null) {
                                String code = codeObject.toString();
                                if (!code.equals("0")) {
                                    Object msgObject = jsonMap.get("msg");
                                    if (msgObject != null) {
                                        String msg = msgObject.toString();
                                        ToastUtils.show(msg);
                                    }
                                    if (clone.code() == 424) {
                                        // 令牌过期回到登录页
                                        LoginActivity.Companion.toLoginActivity(MyApp.context);
                                    }
                                }
                            }
                            body = ResponseBody.create(mediaType, resp);
                            return response.newBuilder().body(body).build();
                        }

                        Logger.d("responseBody\'s content :  maybe [file part] , too large too print , ignored!");
                    }
                }
            }

            Logger.d("============================response\'log============================end");
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return response;
    }

    private void logForRequest(Request request) {
        try {
            String e = request.url().toString();
            Headers headers = request.headers();
            Logger.d("============================request\'log============================");
            Logger.d("method : " + request.method());
            Logger.d("url : " + e);
            if (headers.size() > 0) {
                Logger.d("headers : " + headers.toString());
            }

            RequestBody requestBody = request.body();
            if (requestBody != null) {
                MediaType mediaType = requestBody.contentType();
                if (mediaType != null) {
                    Logger.d("requestBody\'s contentType : " + mediaType.toString());
                    if (this.isText(mediaType)) {
                        Logger.d("requestBody\'s content : " + this.bodyToString(request));
                    } else {
                        Logger.d("requestBody\'s content :  maybe [file part] , too large too print , ignored!");
                    }
                }
            }

            Logger.d("============================request\'log============================end");
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    private boolean isText(MediaType mediaType) {
        return mediaType.type().equals("text") || mediaType.subtype().equals("json") || mediaType.subtype().equals("xml") || mediaType.subtype().equals("html") || mediaType.subtype().equals("webviewhtml");
    }

    private String bodyToString(Request request) {
        try {
            Request e = request.newBuilder().build();
            Buffer buffer = new Buffer();
            assert e.body() != null;
            e.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (IOException var4) {
            return "something error when show requestBody.";
        }
    }
}
