package com.malalisy.algolizer.ui.shortestpathalgorithm

import android.graphics.Point

interface ShortestPathAlgorithmContract {
    interface View {
        fun animateVisitedItems(vararg cells: Pair<Int, Int>)
        fun animateBlockItem(i: Int, j: Int)
        fun animateSourceItem(i: Int, j: Int)
        fun animateDestinationItem(i: Int, j: Int)
        fun animateSolutionCells(vararg cells: Pair<Int, Int>)
        fun animateRemoveSolutionCells(vararg cells: Pair<Int, Int>)
        fun showHideDestinationLabel(show: Boolean)
        fun showHideSourceLabel(show: Boolean)
        fun showHideControls(show: Boolean, anticipateOvershoot: Boolean = true)
        fun showHidePlayButton(show: Boolean)
        fun showHidePauseButton(show: Boolean)
        fun showHideResultContainer(
            show: Boolean,
            solutionFound: Boolean = false,
            solutionCost: Int = 0
        )

        fun showHideAnimationSeekBar(show: Boolean)
        fun setAnimationSeekBarMaxValue(maxValue: Int)
        fun setAnimationSeekBarValue(value: Int)
        fun clearGrid()
        fun animateRemoveVisitedItems(vararg cells: Pair<Int, Int>)
        fun showHideInteractiveModeButton(show: Boolean)
        fun animateRemoveDestinationCell(destination: Pair<Int, Int>)
        fun setInteractiveMode(interactive: Boolean)

        fun getGridDimen(): Point

        fun setGrid(rows: Int, columns: Int)
        fun getCellSize(): Int
    }

    interface Presenter {
        fun setupView(view: View)
        fun onPlayClicked()
        fun onForwardClicked()
        fun onPauseClicked()
        fun onViewPause()
        fun onSpeedChanged(speed: Float)
        fun onRestartClick()
        fun onAnimationSeekBarChanged(value: Int)
        fun onCellStartTouch(i: Int, j: Int)
        fun onCellTouchMove(i: Int, j: Int)
        fun onInteractiveCheckChange(enabled: Boolean)
        fun onAlgorithmSelected(position: Int)
        fun onCloseResultClick()
    }
}