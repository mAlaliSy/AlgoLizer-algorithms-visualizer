package com.malalisy.algolizer.ui.mstalgorithms

import android.os.Handler
import com.malalisy.algolizer.domain.mst.MSTAlgorithmRunner
import com.malalisy.algolizer.domain.mst.MSTAlgorithmsFactory
import com.malalisy.algolizer.domain.mst.MSTAlgorithmsFactory.KRUSKAL_ALGORITHM
import com.malalisy.algolizer.domain.mst.MSTAlgorithmsFactory.PRIMS_ALGORITHM
import java.lang.IllegalArgumentException

class MSTAlgorithmsPresenter : MSTAlgorithmsContract.Presenter {

    companion object {
        private const val EDGE_ANIMATION_DURATION: Long = 300L
    }

    private lateinit var view: MSTAlgorithmsContract.View
    private var algorithmType: Int = KRUSKAL_ALGORITHM
    private var algorithm: MSTAlgorithmRunner? = null

    private var algorithmRun = false
    private lateinit var mst: List<Pair<Int, Int>>
    private lateinit var adjacencyMatrix: Array<Array<Int>>

    private var handler = Handler()

    private var mstAnimationIndex = 0
    private val mstAnimationRunnable: Runnable = object : Runnable {
        override fun run() {
            val edge = mst[mstAnimationIndex]
            view.animateEdge(
                edge.first,
                edge.second,
                adjacencyMatrix[edge.first][edge.second],
                EDGE_ANIMATION_DURATION * 3 / 4
            )
            mstAnimationIndex++
            if (mstAnimationIndex < mst.size) {
                handler.postDelayed(this, EDGE_ANIMATION_DURATION)
            } else {
                handleAnimationEnd()
            }
        }
    }

    private fun handleAnimationEnd() {
        view.showHideControls(false, false)
        view.showSolution(show = true, found = true, cost = algorithm!!.mstCost)
    }


    override fun setupView(view: MSTAlgorithmsContract.View) {
        this.view = view
    }

    override fun onAlgorithmChange(position: Int) {
        algorithmType = when (position) {
            0 -> KRUSKAL_ALGORITHM
            1 -> PRIMS_ALGORITHM
            else -> throw IllegalArgumentException()
        }
    }

    override fun onRunClicked(adjacencyList: List<List<Pair<Int, Int>>>) {
        view.showHideResetButton(false)
        adjacencyMatrix =
            Array(adjacencyList.size) { Array(adjacencyList.size) { 0 } }

        adjacencyList.forEachIndexed { index, list ->
            for (edge in list) {
                adjacencyMatrix[index][edge.first] = edge.second
            }
        }

        algorithm = MSTAlgorithmsFactory.getAlgorithm(algorithmType, adjacencyMatrix)
        algorithm!!.run()

        if (algorithm!!.mst.size != adjacencyMatrix.size - 1) {
            view.showSolution(show = true, found = false, cost = 0)
        } else {
            this.mst = algorithm!!.mst
            view.resetEdges()

            mstAnimationIndex = 0
            handler.postDelayed(mstAnimationRunnable, EDGE_ANIMATION_DURATION)
        }

    }


    override fun onPauseClicked() {

    }

    override fun onRestartClick() {

    }


}