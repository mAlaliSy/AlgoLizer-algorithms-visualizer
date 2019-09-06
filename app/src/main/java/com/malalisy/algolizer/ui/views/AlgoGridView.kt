package com.malalisy.algolizer.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
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
        const val DEFAULT_BACKGROUND_COLOR = 0xFFFFFF
        const val DEFAULT_VISITED_COLOR = 0x03A9F4
        const val DEFAULT_BLOCK_COLOR = 0x3F3A3A
        const val DEFAULT_SOURCE_COLOR = 0x4CAF50
        const val DEFAULT_DESTINATION_COLOR = 0xD81B60
        const val DEFAULT_EMPTY_CELL_COLOR = 0xE0E0E0

        const val DEFAULT_CELL_PADDING = 10
        const val CELL_CORNER_RADIUS = 5f

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

    init {

        /**
         * Set up the grid colors and other attributes from the XML layout attributes
         */
        context?.let {
            val typedArray = it.obtainStyledAttributes(attrs, R.styleable.AlgoGridView)

            bgColor = typedArray.getColor(
                R.styleable.AlgoGridView_bgColor,
                DEFAULT_EMPTY_CELL_COLOR
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
            gridColumns * cellSize + gridColumns * cellPadding,
            gridRows * cellSize + gridRows * cellPadding
        )
    }

    /**
     * Animate the color of the cell at position (i, j) to be a block color
     *
     * @param i the horizontal position of cell
     * @param j the vertical position of cell
     */
    fun animateBlockCell(i: Int, j: Int) {

    }

    /**
     * Animate the color of the cell at position (i, j) to be a visited color
     *
     * @param cells
     */
    fun animateVisitedCells(vararg cells: Pair<Int, Int>) {

    }

    /**
     * Animate the color of the cell at position (i, j) to be a source color
     *
     * @param i the horizontal position of cell
     * @param j the vertical position of cell
     */
    fun animateSourceCell(i: Int, j: Int) {

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
            canvas.drawColor(bgColor)
            drawBackgroundCells(canvas)


        }
    }

    private fun drawBackgroundCells(canvas: Canvas) {
        for (i in 0 until gridRows) {
            for (j in 0 until gridColumns) {

                cellRect.set(
                    j * cellSize + (j + 1) * cellPadding / 2f,
                    i * cellSize + (i + 1) * cellPadding / 2f,
                    (j + 1) * cellSize + (j + 1) * cellPadding / 2f,
                    (i + 1) * cellSize + (i + 1) * cellPadding / 2f
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


}