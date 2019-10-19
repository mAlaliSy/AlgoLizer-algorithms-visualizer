package com.malalisy.algolizer.ui.mstalgorithms

interface MSTAlgorithmsContract {

    interface View {
        fun setAcceptGraphChanges(accept: Boolean)
        fun animateEdge(from: Int, to: Int, weight: Int, duration: Long)
        fun resetEdges()
        fun showSolution(show: Boolean, found: Boolean, cost: Int)
        fun showError(error: String)

        fun showHideResetButton(show: Boolean)
        fun showHideControls(show: Boolean, anticipateOvershoot: Boolean)
    }


    interface Presenter {
        fun setupView(view: View)
        fun onAlgorithmChange(position: Int)
        fun onRunClicked(adjacencyList: List<List<Pair<Int, Int>>>)
        fun onPauseClicked()
        fun onRestartClick()

    }
}