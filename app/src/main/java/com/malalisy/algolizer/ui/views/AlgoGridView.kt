package com.malalisy.algolizer.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.malalisy.algolizer.R
import kotlin.math.min

class AlgoGridView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    companion object {
        val DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT
        val DEFAULT_VISITED_COLOR = Color.parseColor("#03A9F4")
        val DEFAULT_BLOCK_COLOR = Color.parseColor("#3F3A3A")
        val DEFAULT_SOURCE_COLOR = Color.parseColor("#4CAF50")
        val DEFAULT_DESTINATION_COLOR = Color.parseColor("#D81B60")
        val DEFAULT_EMPTY_CELL_COLOR = Color.parseColor("#E0E0E0")

        val DEFAULT_CELL_PADDING = 10
        val CELL_CORNER_RADIUS = 5f

        val DEFAULT_ANIM_DURATION = 500
    }

    /**
     * A list that stores the data for colored cells
     */
    var colorsItems = mutableListOf<GridColorItem>()

    /**
     * A callback for when the user touch and move through grid cells
     */
    var onGridCellSelected: ((i: Int, j: Int) -> Unit)? = null

    /**
     * The colors of different type of cells
     */
    var visitedColor = DEFAULT_VISITED_COLOR
    var blockColor = DEFAULT_BLOCK_COLOR
    var sourceColor = DEFAULT_SOURCE_COLOR
    var destinationColor = DEFAULT_DESTINATION_COLOR
    var emptyCellColor = DEFAULT_EMPTY_CELL_COLOR
    var bgColor = DEFAULT_BACKGROUND_COLOR

    /**
     * A paint for drawing cells
     */
    val cellPaint: Paint

    /**
     * A paint for drawing empty cells
     */
    val emptyCellPaint: Paint

    /**
     * A rect object to be reused for specifying grid cells bounds
     */
    val cellRect: RectF

    /**
     * Number of rows in the grid
     */
    var gridRows = 0

    /**
     * Number of columns in the grid
     */
    var gridColumns = 0

    /**
     * The cell side size, as the cells will be a square
     */
    var cellSize: Int = 0

    /**
     * The cell inset padding
     */
    var cellPadding = DEFAULT_CELL_PADDING

    /**
     * Duration for cells color animation
     */
    var animDuration = DEFAULT_ANIM_DURATION

    init {

        /**
         * Set up the grid colors and other attributes from the XML layout attributes
         */
        context?.let {
            val typedArray = it.obtainStyledAttributes(attrs, R.styleable.AlgoGridView)

            bgColor = typedArray.getColor(
                R.styleable.AlgoGridView_bgColor,
                DEFAULT_BACKGROUND_COLOR
            )

            emptyCellColor = typedArray.getColor(
                R.styleable.AlgoGridView_emptyCellColor,
                DEFAULT_EMPTY_CELL_COLOR
            )

            visitedColor = typedArray.getColor(
                R.styleable.AlgoGridView_visitedCellColor,
                DEFAULT_VISITED_COLOR
            )

            blockColor = typedArray.getColor(
                R.styleable.AlgoGridView_blockCellColor,
                DEFAULT_BLOCK_COLOR
            )

            sourceColor = typedArray.getColor(
                R.styleable.AlgoGridView_sourceCellColor,
                DEFAULT_SOURCE_COLOR
            )

            destinationColor = typedArray.getColor(
                R.styleable.AlgoGridView_destinationCellColor,
                DEFAULT_DESTINATION_COLOR
            )

            gridRows = typedArray.getInt(R.styleable.AlgoGridView_gridRows, 0)
            gridColumns = typedArray.getInt(R.styleable.AlgoGridView_gridColumns, 0)

            cellSize = typedArray.getDimensionPixelSize(R.styleable.AlgoGridView_cellSize, 0)

            cellPadding =
                typedArray.getDimensionPixelSize(
                    R.styleable.AlgoGridView_cellPadding,
                    DEFAULT_CELL_PADDING
                )

            animDuration =
                typedArray.getInt(R.styleable.AlgoGridView_animDuration, DEFAULT_ANIM_DURATION)

            typedArray.recycle()
        }


        cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        emptyCellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = emptyCellColor
        }

        cellRect = RectF()

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        setMeasuredDimension(
            gridColumns * cellSize + (gridColumns + 1) * cellPadding,
            gridRows * cellSize + (gridRows + 1) * cellPadding
        )
    }

    /**
     * Animate the color of the cell at position (i, j) to be a block color
     *
     * @param i the horizontal position of cell
     * @param j the vertical position of cell
     */
    fun animateBlockCell(i: Int, j: Int) {
        animateCellColors(blockColor, i to j)
    }

    /**
     * Animate the color of the cell at position (i, j) to be a visited color
     *
     * @param cells
     */
    fun animateVisitedCells(vararg cells: Pair<Int, Int>) {
        animateCellColors(visitedColor, *cells)
    }

    /**
     * Animate the color of the cell at position (i, j) to be a source color
     *
     * @param i the horizontal position of cell
     * @param j the vertical position of cell
     */
    fun animateSourceCell(i: Int, j: Int) {
        animateCellColors(sourceColor, i to j)
    }

    /**
     * Animate the color of the cell at position (i, j) to be a destination color
     *
     * @param i the horizontal position of cell
     * @param j the vertical position of cell
     */
    fun animateDestinationCell(i: Int, j: Int) {
        animateCellColors(destinationColor, i to j)
    }

    private fun animateCellColors(color: Int, vararg cells: Pair<Int, Int>) {
        val nColorItems = Array(
            cells.size
        ) { GridColorItem(cells[it].first, cells[it].second, color) }
        colorsItems.addAll(
            nColorItems
        )

        ValueAnimator.ofArgb(emptyCellColor, color).apply {
            duration = animDuration.toLong()
            addUpdateListener {
                nColorItems.forEach { gridItem ->
                    gridItem.color = it.animatedValue as Int
                }
                invalidate()
            }
        }.start()
    }

    /**
     * Clear the grid by setting the colors of every cell to be an empty cell
     *
     */
    fun clearGrid() {
        colorsItems.clear()
        invalidate()
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            /**
             * First, draw the background of the grid
             */
            canvas.drawColor(bgColor)

            /**
             * Draw the empty cells
             */
            drawBackgroundCells(canvas)

            /**
             * Last, draw the cells (source, visited, block and destination)
             */
            drawCells(canvas)

        }
    }

    /**
     * Draw the empty cells placeholder
     *
     * @param canvas
     */
    private fun drawBackgroundCells(canvas: Canvas) {
        for (i in 0 until gridRows) {
            for (j in 0 until gridColumns) {

                cellRect.set(
                    j * cellSize + (j + 1) * cellPadding.toFloat(),
                    i * cellSize + (i + 1) * cellPadding.toFloat(),
                    (j + 1) * cellSize + (j + 1) * cellPadding.toFloat(),
                    (i + 1) * cellSize + (i + 1) * cellPadding.toFloat()
                )

                canvas.drawRoundRect(
                    cellRect,
                    CELL_CORNER_RADIUS,
                    CELL_CORNER_RADIUS,
                    emptyCellPaint
                )
            }
        }
    }

    /**
     * Draw the cells in the colorsItems, that is: block, visited, source and destination cells
     *
     * @param canvas
     */
    private fun drawCells(canvas: Canvas) {
        colorsItems.forEach {
            cellRect.set(
                it.j * cellSize + (it.j + 1) * cellPadding.toFloat(),
                it.i * cellSize + (it.i + 1) * cellPadding.toFloat(),
                (it.j + 1) * cellSize + (it.j + 1) * cellPadding.toFloat(),
                (it.i + 1) * cellSize + (it.i + 1) * cellPadding.toFloat()
            )
            cellPaint.color = it.color
            canvas.drawRoundRect(cellRect, CELL_CORNER_RADIUS, CELL_CORNER_RADIUS, cellPaint)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.action) {
            MotionEvent.ACTION_DOWN or MotionEvent.ACTION_MOVE -> {
                onGridCellSelected?.invoke(
                    (event.y / (cellSize + cellPadding)).toInt(),
                    (event.x / (cellSize + cellPadding)).toInt()
                )
            }
        }

        return true
    }
}