package com.sjb.base.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    abstract fun getViewBinding(inflater: LayoutInflater, parent: ViewGroup?): V

    private lateinit var mFragmentProvider: ViewModelProvider

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

    private fun init() {
        initParam()
        initView()
        initData()
        initListener()
        initViewObservable()
        viewModel.uiChangeLiveData.showDialogEvent.observe(this) {
            //showLoadingExt()
            Logger.i("打开弹窗")
        }

        viewModel.uiChangeLiveData.dismissDialogEvent.observe(this) {
            // dismissLoadingExt()
            Logger.i("关闭弹窗")
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
    protected fun <T : ViewModel> getActivityViewModel(modelClass: Class<T>): T {
        return mFragmentProvider[modelClass]
    }

}