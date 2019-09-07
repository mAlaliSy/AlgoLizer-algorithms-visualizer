package com.malalisy.algolizer.ui.shortestpathalgorithm

interface ShortestPathAlgorithmContract {
    interface View {
        fun animateVisitedItems(vararg cells: Pair<Int, Int>)
        fun animateBlockItem(i: Int, j: Int)
        fun animateSourceItem(i: Int, j: Int)
        fun animateDestinationItem(i: Int, j: Int)
        fun hideDestinationLabel()
        fun showDestinationLabel()
        fun hideSourceLabel()
        fun showControls()
        fun showSolution(solution: List<Pair<Int, Int>>)
        fun showNoPathFound()
    }

    interface Presenter {
        fun setupView(view: View)
        fun onItemSelected(i: Int, j: Int)
        fun onPlayClicked()
        fun onForwardClicked()
    }
}