package com.malalisy.algolizer.ui.mstalgorithms

interface MSTAlgorithmsContract {

    interface View {
        fun setAcceptGraphChanges(accept: Boolean)
        fun animateEdge(from: Int, to: Int, duration: Long)
        fun resetEdges()
        fun showHideSolution(show: Boolean, found: Boolean = false, cost: Int= 0)
        fun showError(error: String)

        fun showHideResetButton(show: Boolean)
        fun showHideControls(show: Boolean)
        fun showHidePlayButton(show: Boolean)
        fun showHidePauseButton(show: Boolean)
        fun resetGraph()
        fun resetMSTEdges()
    }


    interface Presenter {
        fun setupView(view: View)
        fun onAlgorithmChange(position: Int)
        fun onRunClicked(adjacencyList: List<List<Pair<Int, Int>>>)
        fun onPauseClicked()
        fun onRestartClick()

        fun onCloseResultClick()
    }
}