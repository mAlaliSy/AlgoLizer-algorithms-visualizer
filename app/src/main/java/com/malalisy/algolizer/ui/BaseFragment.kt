package com.malalisy.algolizer.ui

import android.view.View
import androidx.fragment.app.Fragment
import com.github.florent37.viewanimator.ViewAnimator

abstract class BaseFragment : Fragment() {

    protected fun showHideView(view: View, show: Boolean, duration: Long = 500L) {
        val animate = ViewAnimator.animate(view)
        if (show) {
            view.visibility = View.VISIBLE
            animate.fadeIn()
        }else {
            animate.fadeOut().onStop { view.visibility = View.INVISIBLE }
        }
        animate.duration(duration).start()
    }

}