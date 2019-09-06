package com.malalisy.algolizer.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class AlgoGridView @JvmOverloads constructor(
    context: Context?,
    attr: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attr, defStyle) {

    /**
     * A matrix that stores the color of each cell in the grid
     */
    lateinit var colorsGrid: Array<Array<Int>>

    /**
     * A callback for when the user touch and move through grid cells
     */
    var onGridCellSelected: ((i: Int, j: Int) -> Unit)? = null


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


}