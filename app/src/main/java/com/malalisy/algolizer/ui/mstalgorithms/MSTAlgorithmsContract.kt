package com.malalisy.algolizer.ui.mstalgorithms

interface MSTAlgorithmsContract {

    interface View {
        fun setAcceptGraphChanges(accept: Boolean)
        fun animateEdge(from: Int, to: Int, weight: Int, duration: Int)
        fun resetEdges()
        fun showMSTCost(cost: Int)
        fun showError(error: String)

        fun showHideResetButton(show:Boolean)
    }


    interface Presenter {
        fun onAlgorithmChange(position: Int)
        fun onRunClicked(adjacencyList: List<List<Pair<Int, Int>>>)
    }
}