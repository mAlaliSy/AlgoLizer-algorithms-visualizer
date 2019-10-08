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
import com.malalisy.algolizer.utils.distance

class GraphView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defstyle: Int = 0
) : View(context, attrs, defstyle) {

    companion object {
        /**
         * default radius for the inner & outer circles
         */
        const val DEFAULT_VERTEX_INNER_RADIUS = 50f

        const val DEFAULT_VERTEX_OUTER_RADIUS = 60f
        /**
         * default colors for the inner & outer circles
         */
        val DEFAULT_VERTEX_INNER_COLOR = Color.parseColor("#FF9800")
        val DEFAULT_VERTEX_OUTER_COLOR = Color.parseColor("#FF673AB7")
        /**
         * default color for transitioning between the background color and the color for a vertex
         */
        val DEFAULT_TRANSITION_COLOR = Color.parseColor("#FFF04C7F")

        val DEFAULT_DRAGGING_EDGE_COLOR = Color.parseColor("#cccccc")
        val DEFAULT_EDGE_COLOR = Color.parseColor("#424242")


    }

    private val vertices: MutableList<VertexViewItem> = mutableListOf()

    private var vertexInnerRadius = DEFAULT_VERTEX_INNER_RADIUS
    private var vertexOuterRadius = DEFAULT_VERTEX_OUTER_RADIUS
    private var transitionColor = DEFAULT_TRANSITION_COLOR

    private var vertexInnerColor = DEFAULT_VERTEX_INNER_COLOR
    private var vertexOuterColor = DEFAULT_VERTEX_OUTER_COLOR

    private var edgeColor = DEFAULT_EDGE_COLOR

    private val verticesPaint: Paint
    private val edgesPaint: Paint

    /**
     * The vertex that the user start dragging from it, so he intended to add an edge from it to the
     * vertex he move his finger up on it
     */
    private var draggingVertex: VertexViewItem? = null

    /**
     * The position of the user finger when he moves it after touching a vertex
     */
    private var draggingEdgeFingerPosition: Pair<Float, Float>? = null

    private var draggingEdgeColor = DEFAULT_DRAGGING_EDGE_COLOR
    private val draggingEdgesPaint: Paint


    /**
     * The adjacency list for a weighted graph
     */
    private val adjacencyList = mutableListOf<MutableList<Pair<Int, Int>>>()

    init {
        verticesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
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

    }


    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        canvas?.let {
            drawDraggingEdge(it)

            drawEdges(it)

            drawVertices(it)
        }
    }

    /**
     * draw a dragging edge if the user touched a vertex and start moving his finger,
     * the line should be drawn from the vertex position to the position of his finger
     */
    private fun drawDraggingEdge(canvas: Canvas) {
        if (draggingVertex != null && draggingEdgeFingerPosition != null) {
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

            }
        }
    }

    private fun drawVertices(canvas: Canvas) {
        for (vertex in vertices) {
            verticesPaint.color = vertex.outerColor
            canvas.drawCircle(vertex.x, vertex.y, vertex.outerRadius, verticesPaint)
            verticesPaint.color = vertex.innerColor
            canvas.drawCircle(vertex.x, vertex.y, vertex.innerRadius, verticesPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                for (vertex in vertices) {
                    val distance = vertex distanceTo (event.x to event.y)

                    // The user touched on an already exists vertex => start dragging an edge
                    if (distance < vertexOuterRadius) {
                        draggingVertex = vertex
                        return true
                    }

                    // The user touched a point close to another vertex and they will overlap
                    if (distance < 2 * vertexOuterRadius) return true

                }
                addVertexItem(event.x, event.y)
            }

            MotionEvent.ACTION_MOVE -> {
                if (draggingVertex != null) {
                    draggingEdgeFingerPosition = event.x to event.y
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                // The user was dragging an edge and he left his finger off screen
                if (draggingVertex != null && draggingEdgeFingerPosition != null) {
                    for (vertex in vertices) {
                        val distance = vertex distanceTo draggingEdgeFingerPosition!!


                        // The user left his finger off screen and it was over a vertex, create an edge
                        if (distance < vertexOuterRadius) {
                            // Make sure the two vertices are not connected
                            for (edge in adjacencyList[draggingVertex!!.number]) {
                                if (edge.first == vertex.number) break
                            }
                            adjacencyList[draggingVertex!!.number].add(vertex.number to 0)
                            adjacencyList[vertex.number].add(draggingVertex!!.number to 0)
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

    private fun addVertexItem(x: Float, y: Float) {
        val vertexViewItem = VertexViewItem(
            vertices.size, x, y,
            0f, 0f, 0, 0
        )
        vertices.add(vertexViewItem)
        adjacencyList.add(mutableListOf())

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