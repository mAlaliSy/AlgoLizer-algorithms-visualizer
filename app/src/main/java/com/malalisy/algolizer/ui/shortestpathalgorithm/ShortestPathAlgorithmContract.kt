package com.malalisy.algolizer.ui.shortestpathalgorithm

interface ShortestPathAlgorithmContract {
    interface View {
        fun animateVisitedItems(vararg cells: Pair<Int, Int>)
        fun animateBlockItem(i: Int, j: Int)
        fun animateSourceItem(i: Int, j: Int)
        fun animateDestinationItem(i: Int, j: Int)
        fun animateSolutionCell(i: Int, j: Int)
        fun showHideDestinationLabel(show: Boolean)
        fun showHideSourceLabel(show: Boolean)
        fun showHideControls(show: Boolean)
        fun showHidePlayButton(show: Boolean)
        fun showHidePauseButton(show: Boolean)
        fun showHideResultContainer(
            show: Boolean,
            solutionFound: Boolean = false,
            solutionCost: Int = 0
        )

        fun showHideAnimationSeekBar(show: Boolean)
        fun setAnimationSeekBarMaxValue(maxValue: Int)
        fun setAnimationSeekBarValue(value:Int)

        fun clearGrid()
        fun animateRemoveVisitedItems(vararg cells: Pair<Int, Int>)
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
    }
}