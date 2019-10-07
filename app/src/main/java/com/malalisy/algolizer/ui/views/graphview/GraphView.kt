package com.malalisy.algolizer.ui.views.graphview

import android.animation.*
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator

class GraphView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defstyle: Int = 0
) : View(context, attrs, defstyle) {

    companion object {
        const val DEFAULT_VERTEX_INNER_RADIUS = 50f
        const val DEFAULT_VERTEX_OUTER_RADIUS = 60f
        val DEFAULT_VERTEX_INNER_COLOR = Color.parseColor("#FF9800")
        val DEFAULT_VERTEX_OUTER_COLOR = Color.parseColor("#FF673AB7")
        val DEFAULT_TRANSITION_COLOR = Color.parseColor("#FFF04C7F")


    }

    private val vertices: MutableList<VertexViewItem> = mutableListOf()
    private val verticesPaint: Paint
    private var vertexInnerRadius = DEFAULT_VERTEX_INNER_RADIUS
    private var vertexOuterRadius = DEFAULT_VERTEX_OUTER_RADIUS

    private var transitionColor = DEFAULT_TRANSITION_COLOR
    private var vertexInnerColor = DEFAULT_VERTEX_INNER_COLOR
    private var vertexOuterColor = DEFAULT_VERTEX_OUTER_COLOR

    init {
        verticesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

    }


    override fun draw(canvas: Canvas?) {
        super.draw(canvas)


        drawVertices(canvas)
    }

    private fun drawVertices(canvas: Canvas?) {
        canvas?.let {
            for (vertex in vertices) {
                verticesPaint.color = vertex.outerColor
                it.drawCircle(vertex.x, vertex.y, vertex.outerRadius, verticesPaint)
                verticesPaint.color = vertex.innerColor
                it.drawCircle(vertex.x, vertex.y, vertex.innerRadius, verticesPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                addVertexItem(event.x, event.y)
            }


        }




        return true
    }

    private fun addVertexItem(x: Float, y: Float) {
        val vertexViewItem = VertexViewItem(
            vertices.size, x, y,
            0f, 0f, 0, 0
        )
        vertices.add(vertexViewItem)

        val innerRadiusProperty =
            PropertyValuesHolder.ofFloat("innerRadius", 25f, vertexInnerRadius)
        val outerRadiusProperty =
            PropertyValuesHolder.ofFloat("outerRadius", 0f, vertexOuterRadius)
        val innerColorProperty =
            PropertyValuesHolder.ofObject(
                "innerColor",
                ArgbEvaluator(),
                transitionColor,
                vertexInnerColor
            )
        val outerColorProperty =
            PropertyValuesHolder.ofObject(
                "outerColor",
                ArgbEvaluator(),
                transitionColor,
                vertexOuterColor
            )

        ValueAnimator().apply {
            setValues(
                innerRadiusProperty,
                outerRadiusProperty,
                innerColorProperty,
                outerColorProperty
            )
            duration = 300
            addUpdateListener {
                val innerRadius = it.getAnimatedValue("innerRadius") as Float
                val outerRadius = it.getAnimatedValue("outerRadius") as Float
                val innerColor = it.getAnimatedValue("innerColor") as Int
                val outerColor = it.getAnimatedValue("outerColor") as Int
                vertexViewItem.run {
                    this.innerRadius = innerRadius
                    this.outerRadius = outerRadius
                    this.innerColor = innerColor
                    this.outerColor = outerColor
                }

                invalidate()
            }
            interpolator = AccelerateInterpolator()
        }.start()


    }


    private data class VertexViewItem(
        val number: Int,
        val x: Float,
        val y: Float,
        var innerRadius: Float,
        var outerRadius: Float,
        var innerColor: Int,
        var outerColor: Int
    )
}