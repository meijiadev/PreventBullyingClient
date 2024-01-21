package com.mj.preventbullying.client.ui.activity

import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import com.gyf.immersionbar.ktx.immersionBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.app.AppMvActivity
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.databinding.ActivityKeywordAddBinding
import com.mj.preventbullying.client.http.result.VRecord
import com.mj.preventbullying.client.tool.dismissLoadingExt
import com.mj.preventbullying.client.tool.showLoadingExt
import com.mj.preventbullying.client.ui.dialog.AddVoiceDialog
import com.mj.preventbullying.client.ui.dialog.ItemListDialog
import com.mj.preventbullying.client.ui.viewmodel.AddViewModel
import com.sjb.base.view.SwitchButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Create by MJ on 2024/1/19.
 * Describe :
 */
// 事件等级
const val LEVEL_SERIOUS = 1    // 严重
const val LEVEL_COMMON = 2     // 一般
const val LEVEL_SLIGHT = 3     // 轻微

const val SENSITIVITY_ACCURACY = 1   // 准确
const val SENSITIVITY_COMMON = 2     // 一般
const val SENSITIVITY_FAST = 3      // 快速

class AddKeywordActivity : AppMvActivity<ActivityKeywordAddBinding, AddViewModel>() {
    private var voiceList: List<VRecord> = mutableListOf()

    private var level = LEVEL_COMMON
    private var sensitivity = SENSITIVITY_COMMON
    private var progress = 1100
    private var voiceId: Long? = null
    override fun getViewBinding(): ActivityKeywordAddBinding {
        return ActivityKeywordAddBinding.inflate(layoutInflater)
    }

    override fun initParam() {
        immersionBar {
            //深色字体
            statusBarDarkFont(true)
        }
        binding.titleLl.titleTv.text = "新增关键词"
        binding.orgListTv.text = MyApp.globalEventViewModel.getSchoolName()
        viewModel.getVoiceList()
    }

    override fun initData() {


    }

    override fun initViewObservable() {
        binding.apply {
            titleLl.backIv.setOnClickListener {
                finish()
            }
            officialLl.setOnClickListener {
                showVoiceDialog(it)
            }
            seriousTv.setOnClickListener {
                level = LEVEL_SERIOUS
                restoreLevelDefault()
                seriousTv.shapeDrawableBuilder.setSolidColor(getColor(com.sjb.base.R.color.gold))
                    .intoBackground()

            }
            commonTv.setOnClickListener {
                level = LEVEL_COMMON
                restoreLevelDefault()
                commonTv.shapeDrawableBuilder.setSolidColor(getColor(com.sjb.base.R.color.gold))
                    .intoBackground()
            }
            slightTv.setOnClickListener {
                level = LEVEL_SLIGHT
                restoreLevelDefault()
                slightTv.shapeDrawableBuilder.setSolidColor(getColor(com.sjb.base.R.color.gold))
                    .intoBackground()
            }

            accuracyTv.setOnClickListener {
                sensitivity = SENSITIVITY_ACCURACY
                restoreSSDefault()
                accuracyTv.shapeDrawableBuilder.setSolidColor(getColor(com.sjb.base.R.color.gold))
                    .intoBackground()
                progress = 1300
                seekbarSen.progress = progress

            }
            commonSensitivityTv.setOnClickListener {
                sensitivity = SENSITIVITY_COMMON
                restoreSSDefault()
                commonSensitivityTv.shapeDrawableBuilder.setSolidColor(getColor(com.sjb.base.R.color.gold))
                    .intoBackground()
                progress = 1100
                seekbarSen.progress = progress
            }
            fastSensitivityTv.setOnClickListener {
                sensitivity = SENSITIVITY_FAST
                restoreSSDefault()
                fastSensitivityTv.shapeDrawableBuilder.setSolidColor(getColor(com.sjb.base.R.color.gold))
                    .intoBackground()
                progress = 1000
                seekbarSen.progress = progress

            }

            confirmTv.setOnClickListener {
                val keyword = keywordEt.text.toString().trim()
                if (keyword.isEmpty() || voiceId == null) {
                    toast("请先填写相关信息！")
                    return@setOnClickListener
                }
                val enable = enableBt.isChecked()
                viewModel.addKeyword(keyword, enable, progress, voiceId, level)
                showLoadingExt()
            }
        }
    }

    private fun restoreLevelDefault() {
        binding.apply {
            seriousTv.shapeDrawableBuilder.setSolidColor(getColor(R.color.white)).intoBackground()
            commonTv.shapeDrawableBuilder.setSolidColor(getColor(R.color.white)).intoBackground()
            slightTv.shapeDrawableBuilder.setSolidColor(getColor(R.color.white)).intoBackground()

        }
    }

    private fun restoreSSDefault() {
        binding.apply {
            accuracyTv.shapeDrawableBuilder.setSolidColor(getColor(R.color.white)).intoBackground()
            commonSensitivityTv.shapeDrawableBuilder.setSolidColor(getColor(R.color.white))
                .intoBackground()
            fastSensitivityTv.shapeDrawableBuilder.setSolidColor(getColor(R.color.white))
                .intoBackground()
        }
    }

    private fun showVoiceDialog(v: View) {
        val location = IntArray(2)
        v.getLocationOnScreen(location)
        val y = location[1]
        val itemListDialog = ItemListDialog(this).setVoiceList(voiceList).onVoiceListener {
            binding.officialTv.text = it.text
            voiceId = it.voiceId.toLong()
        }.onAddVListener {
            showAddVoiceDialog()
        }
        XPopup.Builder(this)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .hasShadowBg(false)
            .hasBlurBg(false)
            .isThreeDrag(false)
            .enableDrag(false)
            .isViewMode(true)
            .offsetY(y + v.height)
            .asCustom(itemListDialog)
            .show()
    }

    private fun showAddVoiceDialog() {
        val addVoiceDialog = AddVoiceDialog(this).onConfirm { msg, times ->
            viewModel.addVoice(msg, times)
        }
        XPopup.Builder(this)
            .isViewMode(true)
            .popupAnimation(PopupAnimation.TranslateFromBottom)
            .asCustom(addVoiceDialog)
            .show()
    }

    override fun initView() {
        binding.apply {
            seekbarSen.max = 2000
            seekbarSen.progress = progress
            seekbarSen.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    //Logger.i("$p1,$p2")
                    if (p1 > 800) {
                        numberTv.text = p1.toString()
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    p0?.let {
                        if (it.progress < 700) {
                            seekbarSen.progress = 700
                            toast("灵敏度尽量大于700以上")
                        }
                    }
                }
            })
        }

    }

    override fun initListener() {
        viewModel.voiceListEvent.observe(this) {
            voiceList = it

        }
        viewModel.addKeywordEvent.observe(this) {
            lifecycleScope.launch {
                delay(1500)
                dismissLoadingExt()
                if (it == true) {
                    finish()
                } else {
                    toast("关键字添加失败")
                }
            }
        }
        viewModel.addVoiceEvent.observe(this) {
            if (it == true) {
                toast("语音文案新增成功！")
                viewModel.getVoiceList()
            } else {
                toast("语音文案新增失败")
            }

        }
    }

}