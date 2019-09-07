package com.malalisy.algolizer.ui.shortestpathalgorithm

interface ShortestPathAlgorithmContract {
    interface View {
        fun animateVisitedItems(vararg cells: Pair<Int, Int>)
        fun animateBlockItem(i: Int, j: Int)
        fun animateSourceItem(i: Int, j: Int)
        fun animateDestinationItem(i: Int, j: Int)
        fun showHideDestinationLabel(show: Boolean)
        fun hideSourceLabel()
        fun showControls()
        fun showHidePlayButton(show:Boolean)
        fun showHidePauseButton(show: Boolean)
        fun showSolution(solution: List<Pair<Int, Int>>)
        fun showNoPathFound()
    }

    interface Presenter {
        fun setupView(view: View)
        fun onItemSelected(i: Int, j: Int)
        fun onPlayClicked()
        fun onForwardClicked()
        fun onPauseClicked()
        fun onViewPause()
    }
}