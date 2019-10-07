package com.malalisy.algolizer.ui.views.graphview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GraphView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defstyle: Int = 0
) : View(context, attrs, defstyle) {


    private val vertices: List<VertexViewItem> = listOf()
    private val verticesPaint:Paint

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
            for (vertex in vertices){
                verticesPaint.color = vertex.outerColor
                it.drawCircle(vertex.x, vertex.y, vertex.outerRadius, verticesPaint)
                verticesPaint.color = vertex.innerColor
                it.drawCircle(vertex.x, vertex.y, vertex.innerRadius, verticesPaint)
            }
        }
    }


    private data class VertexViewItem(
        val number: Int,
        val x: Float,
        val y: Float,
        val innerRadius: Float,
        val outerRadius: Float,
        val innerColor: Int,
        val outerColor: Int
    )
}