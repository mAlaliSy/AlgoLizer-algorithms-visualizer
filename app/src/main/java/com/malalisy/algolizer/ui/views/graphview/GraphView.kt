package com.malalisy.algolizer.ui.views.graphview

import android.content.Context
import android.util.AttributeSet
import android.view.View

class GraphView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defstyle: Int = 0
) : View(context, attrs, defstyle) {


    private data class VertexViewItem(
        val number: Int,
        val x: Int,
        val y: Int,
        val innerRadius: Int,
        val outerRadius: Int,
        val innerColor: Int,
        val outerColor: Int
    )
}