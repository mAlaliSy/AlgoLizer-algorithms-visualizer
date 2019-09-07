package com.malalisy.algolizer.ui.shortestpathalgorithm

interface ShortestPathAlgorithmContract {
    interface View {
        fun animateVisitedItems(vararg cells: Pair<Int, Int>)
        fun animateBlockItem(i: Int, j: Int)
        fun animateSourceItem(i: Int, j: Int)
        fun animateDestinationItem(i: Int, j: Int)
        fun animateSolutionCell(i: Int, j: Int)
        fun showHideDestinationLabel(show: Boolean)
        fun hideSourceLabel()
        fun showHideControls(show: Boolean)
        fun showHidePlayButton(show: Boolean)
        fun showHidePauseButton(show: Boolean)
        fun showResultContainer(solutionFound: Boolean, solutionCost: Int = 0)
    }

    interface Presenter {
        fun setupView(view: View)
        fun onItemSelected(i: Int, j: Int)
        fun onPlayClicked()
        fun onForwardClicked()
        fun onPauseClicked()
        fun onViewPause()
        fun onSpeedChanged(speed: Float)
    }
}