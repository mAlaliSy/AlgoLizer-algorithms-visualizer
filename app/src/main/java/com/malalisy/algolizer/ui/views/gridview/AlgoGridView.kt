package com.malalisy.algolizer.ui.views.gridview

import android.animation.*
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.malalisy.algolizer.R

class AlgoGridView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    companion object {
        val DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT
        val DEFAULT_VISITED_COLOR = Color.parseColor("#FF673AB7")
        val DEFAULT_BLOCK_COLOR = Color.parseColor("#3F3A3A")
        val DEFAULT_SOURCE_COLOR = Color.parseColor("#4CAF50")
        val DEFAULT_DESTINATION_COLOR = Color.parseColor("#D81B60")
        val DEFAULT_EMPTY_CELL_COLOR = Color.parseColor("#E0E0E0")
        val DEFAULT_SOLUTION_CELL_COLOR = Color.parseColor("#FF9800")
        val DEFAULT_TRANSITION_COLOR = Color.parseColor("#FFF04C7F")

        val DEFAULT_CELL_MARGIN = 10
        val CELL_CORNER_RADIUS = 15f
        val MAX_CELL_CORNER_RADIUS = 100f

        /**
         * Duration for animating cells
         */
        val DEFAULT_ANIM_DURATION = 250
        /**
         * Duration of starting the clearance of next row
         */
        val CLEAR_DURATION = 30L
    }

    /**
     * A list that stores the data for colored cells
     */
    private var colorsItems = mutableListOf<GridColorItem>()
    /**
     * A list that stores the data for colored cells to be removed
     */
    private var colorsToBeRemovedItems = mutableListOf<GridColorItem>()
    /**
     * A list that stores the data for solution cells
     */
    private var solutionCells = mutableListOf<GridColorItem>()
    /**
     * Used to animate the clearance of grid
     */
    private var clearGridItems = mutableListOf<GridColorItem>()

    private val clearHandler = Handler()

    /**
     * A callback for when the user start to touch a grid cell
     */
    var onGridCellStartTouch: ((i: Int, j: Int) -> Unit)? = null
    /**
     * A callback for when the user move his touch through grid cells
     */
    var onGridCellTouchMove: ((i: Int, j: Int) -> Unit)? = null

    /**
     * The colors of different type of cells
     */
    var visitedColor = DEFAULT_VISITED_COLOR
    var transitionColor = DEFAULT_TRANSITION_COLOR
    var blockColor = DEFAULT_BLOCK_COLOR
    var sourceColor = DEFAULT_SOURCE_COLOR
    var destinationColor = DEFAULT_DESTINATION_COLOR
    var emptyCellColor = DEFAULT_EMPTY_CELL_COLOR
    var solutionColor = DEFAULT_SOLUTION_CELL_COLOR
    var bgColor = DEFAULT_BACKGROUND_COLOR

    /**
     * A paint for drawing cells
     */
    private val cellPaint: Paint

    /**
     * A paint for drawing empty cells
     */
    private val emptyCellPaint: Paint

    /**
     * A rect object to be reused for specifying grid cells bounds
     */
    private val cellRect: RectF

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
     * The cell inset margin
     */
    var cellMargin =
        DEFAULT_CELL_MARGIN

    /**
     * Duration for cells color animation
     */
    var animDuration: Long = DEFAULT_ANIM_DURATION.toLong()

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

            transitionColor = typedArray.getColor(
                R.styleable.AlgoGridView_destinationCellColor,
                DEFAULT_DESTINATION_COLOR
            )

            gridRows = typedArray.getInt(R.styleable.AlgoGridView_gridRows, 0)
            gridColumns = typedArray.getInt(R.styleable.AlgoGridView_gridColumns, 0)

            cellSize = typedArray.getDimensionPixelSize(R.styleable.AlgoGridView_cellSize, 0)

            cellMargin =
                typedArray.getDimensionPixelSize(
                    R.styleable.AlgoGridView_cellMargin,
                    DEFAULT_CELL_MARGIN
                )

            animDuration =
                typedArray.getInt(R.styleable.AlgoGridView_animDuration,
                    DEFAULT_ANIM_DURATION
                )
                    .toLong()

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
            gridColumns * cellSize + (gridColumns + 1) * cellMargin,
            gridRows * cellSize + (gridRows + 1) * cellMargin
        )
    }

    /**
     * Animate the color of the cell at position (i, j) to be a block color
     *
     * @param i the horizontal position of cell
     * @param j the vertical position of cell
     */
    fun animateBlockCell(i: Int, j: Int) {
        animateCellColors(colorsItems, emptyCellColor, blockColor, i to j)
    }

    /**
     * Animate the color of the cell at position (i, j) to be a visited color
     *
     * @param cells
     */
    fun animateVisitedCells(vararg cells: Pair<Int, Int>) {
        animateCellColors(colorsItems, emptyCellColor, visitedColor, *cells)
    }

    /**
     * Animate the color of the cell at position (i, j) to be go back to an empty cell
     *
     * @param i the horizontal position of cell
     * @param j the vertical position of cell
     */

    fun animateRemoveVisitedItems(vararg cells: Pair<Int, Int>) {
        colorsItems.removeAll { eCell ->
            cells.firstOrNull { eCell.i == it.first && eCell.j == it.second } != null
        }
        val items =
            cells.map {
                GridColorItem(
                    it.first,
                    it.second,
                    visitedColor,
                    CELL_CORNER_RADIUS
                )
            }
        colorsToBeRemovedItems.addAll(items)


        val colorProperty = PropertyValuesHolder.ofObject(
            "color",
            ArgbEvaluator(),
            visitedColor,
            emptyCellColor
        )
        val radiusProperty =
            PropertyValuesHolder.ofFloat("radius",
                CELL_CORNER_RADIUS,
                MAX_CELL_CORNER_RADIUS
            )

        ValueAnimator().apply {
            setValues(radiusProperty, colorProperty)
            duration = animDuration
            addUpdateListener {
                val colorValue = it.getAnimatedValue("color") as Int
                val radius = it.getAnimatedValue("radius") as Float
                items.forEach {
                    it.rectRadius = radius
                    it.color = colorValue
                }
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    colorsToBeRemovedItems.removeAll(items)
                }
            })
            interpolator = AccelerateInterpolator()
        }.start()

    }

    /**
     * Animate the color of the cell at position (i, j) to be go back to an empty cell
     *
     * @param i the horizontal position of cell
     * @param j the vertical position of cell
     */

    fun animateRemoveSolutionCells(vararg cells: Pair<Int, Int>) {
        solutionCells.removeAll { eCell ->
            cells.firstOrNull { eCell.i == it.first && eCell.j == it.second } != null
        }
        val items =
            cells.map {
                GridColorItem(
                    it.first,
                    it.second,
                    visitedColor,
                    CELL_CORNER_RADIUS
                )
            }
        colorsToBeRemovedItems.addAll(items)


        val colorProperty = PropertyValuesHolder.ofObject(
            "color",
            ArgbEvaluator(),
            visitedColor,
            Color.TRANSPARENT
        )
        val radiusProperty =
            PropertyValuesHolder.ofFloat("radius",
                CELL_CORNER_RADIUS,
                MAX_CELL_CORNER_RADIUS
            )

        ValueAnimator().apply {
            setValues(radiusProperty, colorProperty)
            duration = animDuration
            addUpdateListener {
                val colorValue = it.getAnimatedValue("color") as Int
                val radius = it.getAnimatedValue("radius") as Float
                items.forEach {
                    it.rectRadius = radius
                    it.color = colorValue
                }
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    colorsToBeRemovedItems.removeAll(items)
                }
            })
            interpolator = AccelerateInterpolator()
        }.start()

    }

    /**
     * Animate the color of the cell at position (i, j) to be go back to an empty cell
     *
     * @param i the horizontal position of cell
     * @param j the vertical position of cell
     */

    fun animateRemoveDestinationCell(cell: Pair<Int, Int>) {
        colorsItems.removeAll { cell.first == it.i && cell.second == it.j }
        val item = GridColorItem(
            cell.first,
            cell.second,
            visitedColor,
            CELL_CORNER_RADIUS
        )
        colorsItems.add(item)


        val colorProperty = PropertyValuesHolder.ofObject(
            "color",
            ArgbEvaluator(),
            visitedColor,
            emptyCellColor
        )
        val radiusProperty =
            PropertyValuesHolder.ofFloat("radius",
                CELL_CORNER_RADIUS,
                MAX_CELL_CORNER_RADIUS
            )

        ValueAnimator().apply {
            setValues(radiusProperty, colorProperty)
            duration = animDuration
            addUpdateListener {
                val colorValue = it.getAnimatedValue("color") as Int
                val radius = it.getAnimatedValue("radius") as Float
                item.rectRadius = radius
                item.color = colorValue

                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    colorsItems.remove(item)
                }
            })
            interpolator = AccelerateInterpolator()
        }.start()

    }

    /**
     * Animate the color of the cell at position (i, j) to be a source color
     *
     * @param i the horizontal position of cell
     * @param j the vertical position of cell
     */
    fun animateSourceCell(i: Int, j: Int) {
        animateCellColors(colorsItems, emptyCellColor, sourceColor, i to j)
    }

    /**
     * Animate the color of the cell at position (i, j) to be a destination color
     *
     * @param i the horizontal position of cell
     * @param j the vertical position of cell
     */
    fun animateDestinationCell(i: Int, j: Int) {
        animateCellColors(colorsItems, emptyCellColor, destinationColor, i to j)
    }


    /**
     * Animate the color of the cell at position (i, j) to be a solution color
     *
     * @param i the horizontal position of cell
     * @param j the vertical position of cell
     */
    fun animateSolutionCells(vararg cells: Pair<Int, Int>) {
        animateCellColors(solutionCells, visitedColor, solutionColor, *cells)
    }

    private fun animateCellColors(
        cellColorItems: MutableList<GridColorItem>,
        startColor: Int,
        color: Int,
        vararg cells: Pair<Int, Int>
    ) {
        val nColorItems = Array(
            cells.size
        ) {
            GridColorItem(
                cells[it].first,
                cells[it].second,
                color,
                MAX_CELL_CORNER_RADIUS
            )
        }
        cellColorItems.addAll(nColorItems)

        val colorProperty = PropertyValuesHolder.ofObject(
            "color",
            ArgbEvaluator(),
            startColor,
            transitionColor,
            color
        )
        val radiusProperty =
            PropertyValuesHolder.ofFloat("radius",
                MAX_CELL_CORNER_RADIUS,
                CELL_CORNER_RADIUS
            )

        ValueAnimator().apply {
            setValues(radiusProperty, colorProperty)
            duration = animDuration
            addUpdateListener {
                val colorValue = it.getAnimatedValue("color") as Int
                val radius = it.getAnimatedValue("radius") as Float
                nColorItems.forEach { gridItem ->
                    gridItem.rectRadius = radius
                    gridItem.color = colorValue
                }
                invalidate()
            }
            interpolator = AccelerateInterpolator()
        }.start()
    }

    /**
     * Clear the grid by setting the colors of every cell to be an empty cell
     *
     */
    private var clearRowIndex = 0
    private var clearRunnable: Runnable = object : Runnable {
        override fun run() {
            if (clearRowIndex >= gridRows) {
                colorsItems.clear()
                solutionCells.clear()
                clearGridItems.clear()
                return
            }
            val newClearCells = mutableListOf<GridColorItem>()
            for (i in 0..gridColumns) {
                newClearCells.add(
                    GridColorItem(
                        clearRowIndex,
                        i,
                        Color.WHITE,
                        CELL_CORNER_RADIUS
                    )
                )
            }
            clearGridItems.addAll(newClearCells)

            ValueAnimator.ofArgb(Color.WHITE, emptyCellColor).apply {
                duration = 250
                addUpdateListener { animator ->
                    newClearCells.forEach {
                        it.color = animator.animatedValue as Int
                    }
                    invalidate()
                }
                start()
            }
            clearRowIndex++
            clearHandler.postDelayed(this,
                CLEAR_DURATION
            )
        }

    }

    fun clearGrid(animate: Boolean) {
        if (animate) {
            clearRowIndex = 0
            clearGridItems.clear()

            clearHandler.postDelayed(clearRunnable,
                CLEAR_DURATION
            )

        } else {
            colorsItems.clear()
            invalidate()
        }
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
             * Draw the cells (source, visited, block and destination)
             */
            drawCellColors(colorsItems, canvas)

            drawCellColors(solutionCells, canvas)


            drawCellColors(colorsToBeRemovedItems, canvas)




            /**
             * Draw the effect of clearing the grid
             *
             */
            drawCellColors(clearGridItems, canvas)

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
                    j * cellSize + (j + 1) * cellMargin.toFloat(),
                    i * cellSize + (i + 1) * cellMargin.toFloat(),
                    (j + 1) * cellSize + (j + 1) * cellMargin.toFloat(),
                    (i + 1) * cellSize + (i + 1) * cellMargin.toFloat()
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
     * Draw a list of GridColorItem on a canvas
     *
     * @param list of GridColorItem
     * @param canvas
     */
    private fun drawCellColors(list: List<GridColorItem>, canvas: Canvas) {
        list.forEach {
            cellRect.set(
                it.j * cellSize + (it.j + 1) * cellMargin.toFloat(),
                it.i * cellSize + (it.i + 1) * cellMargin.toFloat(),
                (it.j + 1) * cellSize + (it.j + 1) * cellMargin.toFloat(),
                (it.i + 1) * cellSize + (it.i + 1) * cellMargin.toFloat()
            )
            cellPaint.color = it.color
            canvas.drawRoundRect(cellRect, it.rectRadius, it.rectRadius, cellPaint)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        /**
         * Discard clicks when clearing the grid
         */
        if (clearGridItems.size > 0) return true
        event?.let {
            val row = (it.y / (cellSize + cellMargin)).toInt()
            val col = (it.x / (cellSize + cellMargin)).toInt()
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    onGridCellStartTouch?.invoke(
                        row, col
                    )
                }
                MotionEvent.ACTION_MOVE -> {
                    onGridCellTouchMove?.invoke(row, col)
                }
                else -> {
                }
            }
        }


        return true
    }


}