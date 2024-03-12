package com.mj.preventbullying.client.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView


/**
 * Create by MJ on 2024/3/12.
 * Describe :
 */

class MarqueTextView : AppCompatTextView {
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    override fun isFocused(): Boolean {
        return true
    }
}