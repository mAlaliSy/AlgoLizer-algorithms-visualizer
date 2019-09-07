package com.malalisy.algolizer.utils

import android.animation.Animator
import android.view.View

fun View.hide(duration: Long) {
    animate().apply {
        alpha(0f)
        this.duration = duration
        withEndAction { alpha = 0f }
    }.start()
}

fun View.show(duration:Long){
    animate().apply {
        alpha(1f)
        this.duration = duration
        withEndAction { alpha = 1f }
    }.start()
}