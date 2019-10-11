package com.malalisy.algolizer.ui.views.graphview

import android.animation.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.malalisy.algolizer.utils.distance
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.InputConnection
import android.text.*
import com.malalisy.algolizer.utils.midPoint


class GraphView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defstyle: Int = 0
) : View(context, attrs, defstyle) {

    companion object {

        // default radius for the inner & outer circles
        const val DEFAULT_VERTEX_INNER_RADIUS = 50f

        const val DEFAULT_VERTEX_OUTER_RADIUS = 60f

        // Long press time in millis
        const val LONG_PRESS_TIME = 400L


        // default colors for the inner & outer circles
        val DEFAULT_VERTEX_INNER_COLOR = Color.parseColor("#FF673AB7")
        val DEFAULT_VERTEX_OUTER_COLOR = Color.parseColor("#FF673AB7")
        val DEFAULT_EDGE_LABEL_BG = Color.parseColor("#FF9800")

        // default color for transitioning between the background color and the color for a vertex
        val DEFAULT_TRANSITION_COLOR = Color.parseColor("#FFF04C7F")

        val DEFAULT_DRAGGING_EDGE_COLOR = Color.parseColor("#cccccc")
        val DEFAULT_EDGE_COLOR = Color.parseColor("#424242")

        val VERTEX_DELETE_CIRCLE_BG = Color.parseColor("#88000000")
        val VERTEX_DELETE_CIRCLE_BG_DRAGGED = Color.parseColor("#88FF0000")
        val VERTEX_DELETE_RADUIS = DEFAULT_VERTEX_OUTER_RADIUS * 1.5f

    }

    private val vertices: MutableList<VertexViewItem> = mutableListOf()

    private var vertexEditingMode: Boolean = false
    private val startVertexEditingModeRunnable: Runnable = Runnable {
        vertexEditingMode = true
        invalidate()
    }

    /**
     * The vertex that the user start dragging from it, so he intended to add an edge from it to the
     * vertex he move his finger up on it
     */
    private var draggingVertex: VertexViewItem? = null

    /**
     * The position of the user finger when he moves it after touching a vertex
     */
    private var draggingEdgeFingerPosition: Pair<Float, Float>? = null

    private var lastEdge: Pair<Int, Int>? = null

    private var lastEdgeWeight = 0
    /**
     * The adjacency list for a weighted graph
     */
    private val adjacencyList = mutableListOf<MutableList<Pair<Int, Int>>>()


    private var vertexInnerRadius = DEFAULT_VERTEX_INNER_RADIUS

    private var vertexOuterRadius = DEFAULT_VERTEX_OUTER_RADIUS
    private var transitionColor = DEFAULT_TRANSITION_COLOR
    private var vertexInnerColor = DEFAULT_VERTEX_INNER_COLOR

    private var vertexOuterColor = DEFAULT_VERTEX_OUTER_COLOR
    private var edgeColor = DEFAULT_EDGE_COLOR

    private val solidPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val edgeWeightBgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = DEFAULT_EDGE_LABEL_BG
    }
    private val vertixLabelPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = 16 * resources.displayMetrics.density
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }
    private val edgeWeightPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = 12 * resources.displayMetrics.density
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }
    private val edgesPaint: Paint
    private var draggingEdgeColor = DEFAULT_DRAGGING_EDGE_COLOR
    private val draggingEdgesPaint: Paint


    init {
        draggingEdgesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 10f
            color = draggingEdgeColor
        }
        edgesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 10f
            color = edgeColor
        }


        isFocusable = true
        setFocusableInTouchMode(true)
        setOnKeyListener { v, keyCode, event ->
            if (event.action != KeyEvent.ACTION_UP) return@setOnKeyListener false

            if (lastEdge == null) return@setOnKeyListener false
            when {
                keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9 -> {
                    if (lastEdgeWeight >= 10) return@setOnKeyListener false

                    lastEdgeWeight = lastEdgeWeight * 10 + (keyCode - KeyEvent.KEYCODE_0)
                }
                keyCode == KeyEvent.KEYCODE_DEL -> {
                    lastEdgeWeight /= 10
                }
                keyCode == KeyEvent.KEYCODE_ENTER -> {
                    if (lastEdgeWeight == 0) return@setOnKeyListener false
                    adjacencyList[lastEdge!!.first].add(lastEdge!!.second to lastEdgeWeight)
                    adjacencyList[lastEdge!!.second].add(lastEdge!!.first to lastEdgeWeight)
                    toggleKeyboard()
                    lastEdge = null
                    lastEdgeWeight = 0

                }


            }
            invalidate()

            false
        }
    }

    override fun onCheckIsTextEditor(): Boolean {
        return true
    }


    override fun onFinishInflate() {
        super.onFinishInflate()
        isFocusableInTouchMode = true
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        val fic = BaseInputConnection(this, false)
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE


        return fic
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (lastEdge != null) return true

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                for (vertex in vertices) {
                    val distance = vertex distanceTo (event.x to event.y)

                    // The user touched on an already exists vertex => start dragging an edge
                    if (distance < vertexOuterRadius) {
                        draggingVertex = vertex
                        handler.postDelayed(startVertexEditingModeRunnable, LONG_PRESS_TIME)
                        return true
                    }

                    // The user touched a point close to another vertex and they will overlap
                    if (distance < 2 * vertexOuterRadius) return true

                }
                addVertexItem(event.x, event.y)
            }

            MotionEvent.ACTION_MOVE -> {
                if (draggingVertex != null) {

                    if (vertexEditingMode) {
                        draggingVertex!!.x = event.x
                        draggingVertex!!.y = event.y

                        invalidate()
                        return true
                    }

                    // Check if it is long press on vertex, change the vertex position
                    if (draggingVertex!!.distanceTo(event.x to event.y) > vertexOuterRadius) {
                        handler.removeCallbacks(startVertexEditingModeRunnable)
                    }

                    draggingEdgeFingerPosition = event.x to event.y

                    /**
                     * Add snapping effect:
                     * when the user hover nearby a vertex, the dragging edge will be snapped
                     * to that vertex
                     */
                    for (vertex in vertices)
                        if (vertex distanceTo draggingEdgeFingerPosition!! < (vertexOuterRadius * 1.4)) {
                            draggingEdgeFingerPosition = vertex.x to vertex.y
                            break
                        }

                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                if (vertexEditingMode) {
                    vertexEditingMode = false
                    draggingVertex = null
                    invalidate()
                    return true
                }

                // The user was dragging an edge and he left his finger off screen
                if (draggingVertex != null && draggingEdgeFingerPosition != null) {
                    for (vertex in vertices) {
                        if (draggingVertex!!.number == vertex.number) continue
                        val distance = vertex distanceTo draggingEdgeFingerPosition!!


                        // The user left his finger off screen and it was over a vertex, create an edge
                        if (distance < vertexOuterRadius) {
                            // Make sure the two vertices are not connected
                            for (edge in adjacencyList[draggingVertex!!.number]) {
                                if (edge.first == vertex.number) break
                            }
                            toggleKeyboard()
                            lastEdge = vertex.number to draggingVertex!!.number
                            break
                        }
                    }


                    draggingVertex = null
                    draggingEdgeFingerPosition = null
                    invalidate()
                }
            }


        }



        return true
    }


    fun toggleKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)

    }

    private fun addVertexItem(x: Float, y: Float) {
        val vertexViewItem = VertexViewItem(
            vertices.size, x, y,
            0f, 0f, 0, 0
        )
        vertices.add(vertexViewItem)
        adjacencyList.add(mutableListOf())

        val innerRadiusProperty =
            PropertyValuesHolder.ofFloat("innerRadius", vertexOuterRadius / 2, vertexInnerRadius)
        val outerRadiusProperty =
            PropertyValuesHolder.ofFloat("outerRadius", vertexInnerRadius / 2, vertexOuterRadius)
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


    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        canvas?.let {
            drawDraggingEdge(it)

            drawEdges(it)

            drawVertices(it)

            drawVertexDeleteCircle(canvas)
        }
    }

    /**
     * draw a dragging edge if the user touched a vertex and start moving his finger,
     * the line should be drawn from the vertex position to the position of his finger
     */
    private fun drawDraggingEdge(canvas: Canvas) {
        // Don't draw the dragging edge if the user is trying to edit the vertex location
        if (!vertexEditingMode && draggingVertex != null && draggingEdgeFingerPosition != null) {
            canvas.drawLine(
                draggingVertex!!.x,
                draggingVertex!!.y,
                draggingEdgeFingerPosition!!.first,
                draggingEdgeFingerPosition!!.second,
                draggingEdgesPaint
            )
        }
    }

    /**
     * draw the edges between vertices using the adjacencyList
     */
    private fun drawEdges(canvas: Canvas) {
        var label: String
        for (i in 0 until adjacencyList.size) {
            for (edge in adjacencyList[i]) {
                val target = vertices[edge.first]
                canvas.drawLine(
                    vertices[i].x,
                    vertices[i].y,
                    target.x,
                    target.y,
                    edgesPaint
                )

                label = (edge.second).toString()
                val pos = midPoint(vertices[i].x, vertices[i].y, target.x, target.y)

                canvas.drawCircle(pos.first, pos.second, vertexOuterRadius / 2, edgeWeightBgPaint)

                drawTextCenter(canvas, pos.first, pos.second, label, edgeWeightPaint)

            }
        }
        lastEdge?.let {
            canvas.drawLine(
                vertices[lastEdge!!.first].x,
                vertices[lastEdge!!.first].y,
                vertices[lastEdge!!.second].x,
                vertices[lastEdge!!.second].y,
                edgesPaint
            )

            label = lastEdgeWeight.toString()
            val pos = midPoint(
                vertices[lastEdge!!.first].x,
                vertices[lastEdge!!.first].y,
                vertices[lastEdge!!.second].x,
                vertices[lastEdge!!.second].y
            )

            canvas.drawCircle(pos.first, pos.second, vertexOuterRadius / 2, edgeWeightBgPaint)

            drawTextCenter(canvas, pos.first, pos.second, label, edgeWeightPaint)
        }


    }

    private fun drawVertices(canvas: Canvas) {
        var label: String
        for (vertex in vertices) {
            solidPaint.color = vertex.outerColor
            val raduis =
                if (vertexEditingMode && vertex == draggingVertex) vertexOuterRadius * 1.2f else vertex.outerRadius
            canvas.drawCircle(vertex.x, vertex.y, raduis, solidPaint)
//            solidPaint.color = vertex.innerColor
//            canvas.drawCircle(vertex.x, vertex.y, vertex.innerRadius, solidPaint)
            label = (vertex.number + 1).toString()

            drawTextCenter(canvas, vertex.x, vertex.y, label, vertixLabelPaint)

        }
    }

    private fun drawVertexDeleteCircle(canvas: Canvas) {
        if (vertexEditingMode) {
            val x = width / 2f
            val y = height - 3 * VERTEX_DELETE_RADUIS
            var raduis: Float


            if (draggingVertex!!.distanceTo(x to y) < VERTEX_DELETE_RADUIS + vertexOuterRadius) {
                solidPaint.color = VERTEX_DELETE_CIRCLE_BG_DRAGGED
                raduis = VERTEX_DELETE_RADUIS * 1.3f
            } else {
                solidPaint.color = VERTEX_DELETE_CIRCLE_BG
                raduis = VERTEX_DELETE_RADUIS
            }

            canvas.drawCircle(x, y, raduis, solidPaint)
            drawTextCenter(canvas, x, y, "x", vertixLabelPaint)
        }
    }


    private fun drawTextCenter(canvas: Canvas, x: Float, y: Float, text: String, paint: Paint) {
        canvas.drawText(
            text,
            x,
            y - (paint.ascent() + paint.descent()) / 2,
            paint
        )
    }


    private data class VertexViewItem(
        val number: Int,
        var x: Float,
        var y: Float,
        var innerRadius: Float,
        var outerRadius: Float,
        var innerColor: Int,
        var outerColor: Int
    ) {
        infix fun distanceTo(vertexViewItem: VertexViewItem) =
            distance(
                x.toDouble(),
                y.toDouble(),
                vertexViewItem.x.toDouble(),
                vertexViewItem.y.toDouble()
            )

        infix fun distanceTo(pos: Pair<Float, Float>) =
            distance(
                x.toDouble(),
                y.toDouble(),
                pos.first.toDouble(),
                pos.second.toDouble()
            )

    }
}

