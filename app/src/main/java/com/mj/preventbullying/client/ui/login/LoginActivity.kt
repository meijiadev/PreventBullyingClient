package com.mj.preventbullying.client.ui.login

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import androidx.lifecycle.Observer
import cn.jpush.android.cache.Sp
import cn.jpush.android.ups.JPushUPSManager
import coil.load
import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.shape.view.ShapeEditText
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.NetworkUtil
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.SpManager
import com.mj.preventbullying.client.databinding.ActivityLoginBinding
import com.mj.preventbullying.client.ui.MainActivity
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvActivity
import java.util.Random
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * desc:登录页
 * time:2023/12/9
 * author:MJ
 */
class LoginActivity : BaseMvActivity<ActivityLoginBinding, LoginViewModel>() {
    private val nameTV by lazy { binding.username as ShapeEditText }
    private val passwordTv by lazy { binding.password as ShapeEditText }
    private val codeTv by lazy { binding.codeTv as ShapeEditText }
    private val login by lazy { binding.login }
    private lateinit var loginViewModel: LoginViewModel

    private var randomStr: String? = null

    companion object {

        fun toLoginActivity(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            if (context is Application) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    override fun getViewBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }


    @SuppressLint("ResourceType")
    override fun initParam() {
        loginViewModel = getActivityViewModel(LoginViewModel::class.java)

    }

    override fun initData() {
        XXPermissions.with(this)
            // .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .permission(Permission.RECORD_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                    Logger.i("录音权限获取成功")
                    if (all) {

                    }

                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                    super.onDenied(permissions, never)
                    Logger.i("权限获取失败")
                }
            })


    }

    override fun initViewObservable() {
        nameTV.afterTextChanged {
            viewModel.loginDataChanged(
                nameTV.text.toString(),
                passwordTv.text.toString(),
                codeTv.text.toString()
            )
        }

        passwordTv.apply {
            afterTextChanged {
                viewModel.loginDataChanged(
                    nameTV.text.toString(),
                    passwordTv.text.toString(),
                    codeTv.text.toString()
                )
            }
        }

        codeTv.apply {
            afterTextChanged {
                viewModel.loginDataChanged(
                    nameTV.text.toString(),
                    passwordTv.text.toString(),
                    codeTv.text.toString()
                )
            }
        }

    }

    override fun initView() {
        viewModel.loginDataChanged(
            nameTV.text.toString(),
            passwordTv.text.toString(),
            codeTv.text.toString()
        )
        postDelayed({
            Logger.i("网络是否可以：${NetworkUtil.isAvailable()},IP:${NetworkUtil.getIPAddress(true)}")
            if (!NetworkUtil.isAvailable()) {
                toast("网络不可用！")
            }
            randomStr = generateRandomString()
            val url = "http://cloud.zyq0407.com:8080/api/code?randomStr=$randomStr"
            binding.codeImage.load(url)
            Logger.i("加载验证码：$url")
        }, 200)
    }

    override fun initListener() {
        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                nameTV.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                passwordTv.error = getString(loginState.passwordError)
            }
            if (loginState.codeError != null) {
                codeTv.error = getString(loginState.codeError)
            }
        })
        // 登录返回值
        loginViewModel.loginResult.observe(this) {
            toast("登陆成功！")
            SpManager.putString(Constant.ACCESS_TOKEN_KEY, it.access_token)
            SpManager.putString(Constant.FRESH_TOKEN_KEY, it.refresh_token)
            SpManager.putString(Constant.EXPIRES_TIME_KEY, it.expires_in)
            SpManager.putLong(Constant.LOGIN_OR_REFRESH_TIME_KEY, System.currentTimeMillis())
            SpManager.putString(Constant.USER_ID_KEY, it.user_id)
            JPushUPSManager.turnOnPush(this) {
                Logger.i("打开极光推送服务：$it")
            }
            startActivity(MainActivity::class.java)
            finish()
        }

    }


    /**
     * 刷新图片验证码点击事件
     */
    fun refresh(v: View) {
        randomStr = generateRandomString()
        val url = "http://cloud.zyq0407.com:8080/api/code?randomStr=$randomStr"
        //val url="https://login.sina.com.cn/cgi/pin.php?r=9967937&s=0&p=gz-d0dc363f6a4523cbd602a5a10f00c59b"
        binding.codeImage?.load(url) {
            error(R.drawable.ic_launcher_background)
        }
        Logger.i("加载验证码：$url")
    }

    /**
     * 登录点击事件
     */
    fun onLogin(v: View) {
        if (login.isEnabled) {
            val psTV = passwordTv.text.toString()
            val ps = encrypt("thanks,pig4cloud", psTV).trim()
            val name = nameTV.text.toString()
            val code = codeTv?.text.toString()
            Logger.i("加密后的密码：$ps")
            randomStr?.let {
                loginViewModel.login(name, it, code, ps)
            }
        } else {
            toast("请完整填写登录账号密码和验证码")
        }


    }

    /**
     * 密码加密
     */
    private fun encrypt(key: String, data: String): String {
        val cipher = Cipher.getInstance("AES/CFB/NoPadding")
        val secretKeySpec = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(key.toByteArray()))
        val encrypted = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

}

/**
 * 用于简化对EditText组件设置afterTextChanged操作的扩展函数.
 */
fun ShapeEditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

/**
 * 生成随机字符串
 */
fun generateRandomString(length: Int = 10): String {
    val random = Random()
    val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..length)
        .map { _ -> characters[random.nextInt(characters.length)] }
        .joinToString("")
}
