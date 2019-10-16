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
import android.view.animation.AccelerateDecelerateInterpolator
import com.malalisy.algolizer.utils.midPoint
import kotlin.math.min


class GraphView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defstyle: Int = 0
) : View(context, attrs, defstyle) {

    companion object {

        const val DEFAULT_VERTEX_RADIUS = 50f

        // Long press time in millis
        const val LONG_PRESS_TIME = 400L

        val DEFAULT_VERTEX_COLOR = Color.parseColor("#FF673AB7")
        val DEFAULT_EDGE_LABEL_BG = Color.parseColor("#FF9800")

        // default color for transitioning between the background color and the color for a vertex
        val DEFAULT_TRANSITION_COLOR = Color.parseColor("#FFF04C7F")

        val DEFAULT_DRAGGING_EDGE_COLOR = Color.parseColor("#cccccc")
        val DEFAULT_EDGE_COLOR = Color.parseColor("#424242")

        val VERTEX_DELETE_CIRCLE_BG = Color.parseColor("#AA000000")
        val VERTEX_DELETE_CIRCLE_BG_DRAGGED = Color.parseColor("#44FF0000")
        const val VERTEX_DELETE_RADIUS = DEFAULT_VERTEX_RADIUS * 1.5f
        const val VERTEX_DELETE_RADIUS_DRAGGED = VERTEX_DELETE_RADIUS * 1.3f
    }

    private val vertices: MutableList<VertexViewItem> = mutableListOf()

    /*
    * editing vertex mode is the mode where the user can drag a vertex (change its location) or
    * */
    private var vertexEditingMode: Boolean = false

    /*
     * deleteVertexCircle is circle that shows when the user long press a vertex
     * when the user drag the vertex to the circle, the vertex will be deleted
     */
    private var deleteVertexCircleLocation: Pair<Float, Float> = 0f to 0f
    private var deleteVertexCircleBaseLocation: Pair<Float, Float> = 0f to 0f
    private var deleteVertexMaxVerticalDisplacement: Float = 0f
    private var deleteVertexMaxHorizontalDisplacement: Float = 0f
    private var deleteVertexMaxY: Float = 0f
    private var deleteVertexCircleRadius = VERTEX_DELETE_RADIUS / 2f
    private var deleteVertexCircleBg = VERTEX_DELETE_CIRCLE_BG
    private var vertexDraggedToDelete = false

    private val startVertexEditingModeRunnable: Runnable = Runnable {
        vertexEditingMode = true
        deleteVertexCircleRadius = VERTEX_DELETE_RADIUS

        ValueAnimator.ofArgb(Color.TRANSPARENT, VERTEX_DELETE_CIRCLE_BG).run {
            duration = 400L
            addUpdateListener {
                deleteVertexCircleBg = it.animatedValue as Int
                invalidate()
            }
            interpolator = AccelerateInterpolator()
            start()
        }

        animateDeletionCircleScalingEffect()

        updateDeleteVertexCircleLocation()
    }

    /*
     * The vertex that the user start dragging from it, so he intended to add an edge from it to the
     * vertex he move his finger up on it
     */
    private var draggingVertex: VertexViewItem? = null

    /*
     * The position of the user finger when he moves it after touching a vertex
     */
    private var draggingEdgeFingerPosition: Pair<Float, Float>? = null

    private var lastEdge: Pair<Int, Int>? = null

    private var lastEdgeWeight = 0

    /*
     * The adjacency list for a weighted graph
     */
    private val adjacencyList = mutableListOf<MutableList<Pair<Int, Int>>>()
    private var vertexRadius = DEFAULT_VERTEX_RADIUS


    private var vertexColor = DEFAULT_VERTEX_COLOR

    private var transitionColor = DEFAULT_TRANSITION_COLOR

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

        viewTreeObserver.addOnGlobalLayoutListener {
            deleteVertexCircleBaseLocation = width / 2f to height - VERTEX_DELETE_RADIUS * 2
            deleteVertexCircleLocation = deleteVertexCircleBaseLocation
            deleteVertexMaxHorizontalDisplacement = width * 0.2f
            deleteVertexMaxVerticalDisplacement = height * 0.15f

            deleteVertexMaxY = height - VERTEX_DELETE_RADIUS * 1.1f
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
                    if (distance < vertexRadius) {
                        draggingVertex = vertex
                        handler.postDelayed(startVertexEditingModeRunnable, LONG_PRESS_TIME)
                        return true
                    }

                    // The user touched a point close to another vertex and they will overlap
                    if (distance < 2 * vertexRadius) return true
                }
                addVertexItem(event.x, event.y)
            }

            MotionEvent.ACTION_MOVE -> {
                /*
                 * a vertex already picked(clicked), either to create an edge
                 * or to move/delete the vertex
                 */
                if (draggingVertex != null) {
                    // the vertex was long-pressed, the editing (moving/deletion) mode is enabled
                    if (vertexEditingMode) {
                        val wasDragged = vertexDraggedToDelete
                        // check if the dragging vertex is close enough to the deletion circle
                        vertexDraggedToDelete = isVertexDraggedToDelete(event.x, event.y)
                        if (vertexDraggedToDelete) {
                            if (!wasDragged) {
                                animateDeletionCircleDragging(true)
                            }
                        } else {
                            draggingVertex!!.x = event.x
                            draggingVertex!!.y = event.y

                            updateDeleteVertexCircleLocation()
                            if (wasDragged) {
                                animateDeletionCircleDragging(false)
                            }
                        }
                        invalidate()
                        return true
                    }

                    // Check if it is long press on vertex, change the vertex position
                    if (draggingVertex!!.distanceTo(event.x to event.y) > vertexRadius) {
                        handler.removeCallbacks(startVertexEditingModeRunnable)
                    }

                    draggingEdgeFingerPosition = event.x to event.y
                    /*
                     * Add snapping effect:
                     * when the user hover nearby a vertex, the dragging edge will be snapped
                     * to that vertex
                     */
                    for (vertex in vertices) {
                        if (vertex == null) continue
                        if (vertex distanceTo draggingEdgeFingerPosition!! < (vertexRadius * 1.4)) {
                            draggingEdgeFingerPosition = vertex.x to vertex.y
                            break
                        }
                    }
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                if (vertexEditingMode) {
                    if (vertexDraggedToDelete) {
                        deleteDraggingVertex()
                        vertexDraggedToDelete = false
                    }


                    vertexEditingMode = false
                    draggingVertex = null
                    draggingEdgeFingerPosition = null
                    deleteVertexCircleLocation = deleteVertexCircleBaseLocation
                    invalidate()
                    return true
                }

                handler.removeCallbacks(startVertexEditingModeRunnable)

                // The user was dragging an edge and he left his finger off screen
                if (draggingVertex != null && draggingEdgeFingerPosition != null) {
                    for (vertex in vertices) {
                        if (vertex == null) continue
                        if (draggingVertex!!.number == vertex.number) continue
                        val distance = vertex distanceTo draggingEdgeFingerPosition!!

                        // The user left his finger off screen and it was over a vertex, create an edge
                        if (distance < vertexRadius) {
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

    private fun animateDeletionCircleDragging(toDragged: Boolean) {
        val toRadius: Float
        val toColor: Int

        if (toDragged) {
            toRadius = VERTEX_DELETE_RADIUS_DRAGGED
            toColor = VERTEX_DELETE_CIRCLE_BG_DRAGGED
        } else {
            toRadius = VERTEX_DELETE_RADIUS
            toColor = VERTEX_DELETE_CIRCLE_BG
        }

        val radiusHolder =
            PropertyValuesHolder.ofFloat("radius", deleteVertexCircleRadius, toRadius)
        val colorHolder =
            PropertyValuesHolder.ofObject("color", ArgbEvaluator(), deleteVertexCircleBg, toColor)

        ValueAnimator().apply {
            setValues(radiusHolder, colorHolder)
            duration = 200
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                deleteVertexCircleRadius = it.getAnimatedValue(radiusHolder.propertyName) as Float
                deleteVertexCircleBg = it.getAnimatedValue(colorHolder.propertyName) as Int
                invalidate()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    if (!toDragged) animateDeletionCircleScalingEffect()
                }

            })
            start()
        }
    }

    private fun animateDeletionCircleScalingEffect() {
        ValueAnimator.ofFloat(deleteVertexCircleRadius, VERTEX_DELETE_RADIUS * 1.1f)
            .run {
                duration = 500L
                addUpdateListener {
                    if (!vertexEditingMode || vertexDraggedToDelete) cancel()
                    deleteVertexCircleRadius = it.animatedValue as Float
                    invalidate()
                }
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
    }


    private fun deleteDraggingVertex() {
        val vertexIndex = draggingVertex!!.number
        vertices.removeAt(vertexIndex)
        adjacencyList.removeAt(vertexIndex)

        for (edges in adjacencyList) {
            val edgesToAdd = mutableListOf<Pair<Int, Int>>()
            edges.forEach { edge ->
                if (edge.first > vertexIndex) {
                    val newEdge = (edge.first - 1) to edge.second
                    edgesToAdd.add(newEdge)
                }
            }
            edges.removeAll { it.first >= vertexIndex }
            edges.addAll(edgesToAdd)
        }

    }

    private fun updateDeleteVertexCircleLocation() {
        val vertexRelativeToCenter = draggingVertex!!.x - width / 2f
        val vertexPercentToCenter = vertexRelativeToCenter / (width / 2)
        val deleteCircleX = deleteVertexCircleBaseLocation.first +
                deleteVertexMaxHorizontalDisplacement * vertexPercentToCenter

        val vertexToHeight = draggingVertex!!.y / height
        val deleteCircleY = deleteVertexCircleBaseLocation.second -
                deleteVertexMaxVerticalDisplacement * vertexToHeight

        deleteVertexCircleLocation = Pair(deleteCircleX, min(deleteCircleY, deleteVertexMaxY))
    }


    fun toggleKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)

    }

    private fun addVertexItem(x: Float, y: Float) {
        val vertexViewItem = VertexViewItem(vertices.size, x, y, 0f, 0)
        vertices.add(vertexViewItem)
        adjacencyList.add(mutableListOf())

        val radiusProperty =
            PropertyValuesHolder.ofFloat("radius", vertexRadius / 2, vertexRadius)

        val colorProperty =
            PropertyValuesHolder.ofObject(
                "color",
                ArgbEvaluator(),
                transitionColor,
                vertexColor
            )

        ValueAnimator().apply {
            setValues(
                radiusProperty,
                colorProperty
            )
            duration = 300
            addUpdateListener {
                vertexViewItem.run {
                    this.radius = it.getAnimatedValue("radius") as Float
                    this.color = it.getAnimatedValue("color") as Int
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

            if (vertexEditingMode) {
                drawDraggingVertex(canvas)
                drawVertexDeleteCircle(canvas)
            }

        }
    }

    private fun drawDraggingVertex(canvas: Canvas) {
        val x: Float
        val y: Float
        if (vertexDraggedToDelete) {
            x = deleteVertexCircleLocation.first
            y = deleteVertexCircleLocation.second
        } else {
            x = draggingVertex!!.x
            y = draggingVertex!!.y
        }
        solidPaint.color = draggingVertex!!.color
        canvas.drawCircle(x, y, draggingVertex!!.radius * 1.2f, solidPaint)
        drawTextCenter(canvas, x, y, (draggingVertex!!.number + 1).toString(), vertixLabelPaint)
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
            val x: Float
            val y: Float

            if (vertexEditingMode && vertices[i] == draggingVertex && vertexDraggedToDelete) {
                x = deleteVertexCircleLocation.first
                y = deleteVertexCircleLocation.second
            } else {
                x = vertices[i].x
                y = vertices[i].y
            }

            for (edge in adjacencyList[i]) {
                val target = vertices[edge.first]
                val x2: Float
                val y2: Float
                if (vertexEditingMode && target == draggingVertex && vertexDraggedToDelete) {
                    x2 = deleteVertexCircleLocation.first
                    y2 = deleteVertexCircleLocation.second
                } else {
                    x2 = target.x
                    y2 = target.y
                }
                canvas.drawLine(x, y, x2, y2, edgesPaint)
                label = (edge.second).toString()
                val pos = midPoint(x, y, x2, y2)

                canvas.drawCircle(pos.first, pos.second, vertexRadius / 2, edgeWeightBgPaint)

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

            canvas.drawCircle(pos.first, pos.second, vertexRadius / 2, edgeWeightBgPaint)

            drawTextCenter(canvas, pos.first, pos.second, label, edgeWeightPaint)
        }


    }

    private fun drawVertices(canvas: Canvas) {
        for (vertex in vertices) {
            if (vertexEditingMode && vertex == draggingVertex || vertex == null) continue
            drawVertex(canvas, vertex)
        }
    }

    private fun drawVertex(canvas: Canvas, vertex: VertexViewItem) {
        solidPaint.color = vertex.color
        canvas.drawCircle(vertex.x, vertex.y, vertex.radius, solidPaint)
        drawTextCenter(canvas, vertex.x, vertex.y, (vertex.number + 1).toString(), vertixLabelPaint)
    }

    private fun drawVertexDeleteCircle(canvas: Canvas) {

        solidPaint.color = deleteVertexCircleBg

        canvas.drawCircle(
            deleteVertexCircleLocation.first,
            deleteVertexCircleLocation.second,
            deleteVertexCircleRadius,
            solidPaint
        )

        if (!vertexDraggedToDelete) {
            // Only draw the "x" sign when no dragged vertex
            drawTextCenter(
                canvas,
                deleteVertexCircleLocation.first,
                deleteVertexCircleLocation.second,
                "x",
                vertixLabelPaint
            )
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

    private fun isVertexDraggedToDelete(x: Float, y: Float) =
        distance(
            x.toDouble(),
            y.toDouble(),
            deleteVertexCircleLocation.first.toDouble(),
            deleteVertexCircleLocation.second.toDouble()
        ) < VERTEX_DELETE_RADIUS + vertexRadius


    private data class VertexViewItem(
        val number: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var color: Int
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

