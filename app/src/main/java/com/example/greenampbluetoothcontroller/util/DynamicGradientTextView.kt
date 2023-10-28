package com.example.greenampbluetoothcontroller.util

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class DynamicGradientTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var startColor = Color.GREEN
    private var endColor = Color.BLACK

    fun setGradientColors(startColor: Int, endColor: Int) {
        this.startColor = startColor
        this.endColor = endColor
        applyGradient()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            applyGradient()
        }
    }

    private fun applyGradient() {
        val width = right - left
        val height = bottom - top

        // Create a LinearGradient shader with the dynamically set colors
        val shader = LinearGradient(0f, height.toFloat(), 0f, 0f, startColor, endColor, Shader.TileMode.CLAMP)

        paint.shader = shader
    }
}
