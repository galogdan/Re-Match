package com.example.re_match.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.example.re_match.databinding.ViewLoadingBinding

class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewLoadingBinding
    private var animator: ValueAnimator? = null

    init {
        binding = ViewLoadingBinding.inflate(LayoutInflater.from(context), this, true)
        visibility = View.GONE
    }

    fun startLoading() {
        if (visibility != View.VISIBLE) {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(300).start()
            animateLogo()
        }
    }

    fun stopLoading() {
        if (visibility == View.VISIBLE) {
            animate().alpha(0f).setDuration(300).withEndAction {
                visibility = View.GONE
                animator?.cancel()
            }.start()
        }
    }

    private fun animateLogo() {
        animator = ValueAnimator.ofFloat(1f, 1.2f, 1f).apply {
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                binding.ivLogo.scaleX = value
                binding.ivLogo.scaleY = value
            }
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
        }
        animator?.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}