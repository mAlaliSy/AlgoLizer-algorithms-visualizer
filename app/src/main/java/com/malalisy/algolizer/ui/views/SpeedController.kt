package com.malalisy.algolizer.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.Button
import com.malalisy.algolizer.R

class SpeedController @JvmOverloads constructor(
    context: Context?,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : Button(context, attributeSet, defStyle) {

    companion object {
        const val DEFAULT_MAX_SPEED = 4f
        const val DEFAULT_MIN_SPEED = 0.25f
        const val DEFAULT_STEP_SIZE = 1f
        const val DEFAULT_SMALL_STEP_SIZE = 0.25f
    }

    var maxSpeed = DEFAULT_MAX_SPEED
    var minSpeed = DEFAULT_MIN_SPEED
    var stepSize = DEFAULT_STEP_SIZE
    /**
     * Small step size is the step size when the speed is less than 1
     */
    var smallStepSize = DEFAULT_SMALL_STEP_SIZE

    var onSpeedChangeListener: ((speed: Float) -> Unit)? = null

    var currentSpeed = 1.0f

    init {
        /**
         * Obtaining the attributes from xml
         */
        context?.let {
            val attrs = it.obtainStyledAttributes(attributeSet, R.styleable.SpeedController)
            maxSpeed = attrs.getFloat(R.styleable.SpeedController_maxSpeed, DEFAULT_MAX_SPEED)
            minSpeed = attrs.getFloat(R.styleable.SpeedController_minSpeed, DEFAULT_MIN_SPEED)
            stepSize = attrs.getFloat(R.styleable.SpeedController_stepSize, DEFAULT_STEP_SIZE)
            smallStepSize = attrs.getFloat(R.styleable.SpeedController_stepSize, DEFAULT_SMALL_STEP_SIZE)
            attrs.recycle()
        }
        /**
         * Center the text
         */
        gravity = Gravity.CENTER
        /**
         * Set the initial text
         */
        text = "x$currentSpeed"

        setOnClickListener {
            currentSpeed += if (currentSpeed < 1) smallStepSize else stepSize
            // Reset the speed if it exceeded the max speed
            if (currentSpeed > maxSpeed) currentSpeed = minSpeed
            text = "x$currentSpeed"
            onSpeedChangeListener?.invoke(currentSpeed)
        }
    }
}