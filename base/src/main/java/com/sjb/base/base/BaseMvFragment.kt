package com.sjb.base.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.orhanobut.logger.Logger
import com.sjb.base.action.HandlerAction
import com.sjb.base.action.ToastAction
import com.sjb.base.ext.getVmClazz

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */

abstract class BaseMvFragment<V : ViewBinding, VM : BaseViewModel> : Fragment(), ToastAction,
    HandlerAction,
    IBaseView {
    protected lateinit var binding: V
    protected lateinit var viewModel: VM
    protected lateinit var mActivity: AppCompatActivity

    abstract fun getViewBinding(inflater: LayoutInflater, parent: ViewGroup?): V

    private var mActivityProvider: ViewModelProvider? = null

    private var mFragmentProvider: ViewModelProvider? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getViewBinding(inflater, container)
        viewModel = createViewModel()
        mFragmentProvider = ViewModelProvider(this)
        init()
        return binding.root
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as AppCompatActivity
    }

    private fun init() {
        initParam()
        initView()
        initData()
        initListener()
        initViewObservable()
        viewModel.uiChangeLiveData.showDialogEvent.observe(this) {
            showLoadingExt()
            Logger.i("打开弹窗")
        }

        viewModel.uiChangeLiveData.dismissDialogEvent.observe(this) {
            Logger.i("关闭弹窗")
            dismissLoadingExt()
        }
    }


    /**
     *创建ViewModel对象
     */
    private fun createViewModel(): VM {
        return ViewModelProvider(this).get(getVmClazz(this))
    }

    /**
     * 获取Activity作用域的ViewModel
     */
    protected fun <T : ViewModel> getActivityViewModel(modelClass: Class<T>): T? {
        if (mActivityProvider == null) {
            mActivityProvider = ViewModelProvider(mActivity)
        }
        return mActivityProvider?.get(modelClass)
    }

    /**
     * 获取作用域为Fragment的ViewModel
     */
    protected fun <T : ViewModel> getFragmentViewModel(modelClass: Class<T>): T? {
        if (mFragmentProvider == null) {
            mFragmentProvider = ViewModelProvider(this)
        }
        return mFragmentProvider?.get(modelClass)
    }

    /**
     * 跳转 Activity 简化版
     */
    open fun startActivity(clazz: Class<out Activity?>?) {
        mActivity.startActivity(Intent(mActivity, clazz))
    }
}