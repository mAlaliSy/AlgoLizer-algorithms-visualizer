package com.malalisy.algolizer.ui.traversalalgorithms

import android.os.Handler
import com.malalisy.algolizer.domain.mst.MSTAlgorithmsFactory.KRUSKAL_ALGORITHM
import com.malalisy.algolizer.domain.mst.MSTAlgorithmsFactory.PRIMS_ALGORITHM
import com.malalisy.algolizer.domain.traversalalgrotihms.TraversalAlgorithmRunner
import com.malalisy.algolizer.domain.traversalalgrotihms.TraversalAlgorithmsFactory
import com.malalisy.algolizer.domain.traversalalgrotihms.TraversalAlgorithmsFactory.Companion.BFS
import com.malalisy.algolizer.domain.traversalalgrotihms.TraversalAlgorithmsFactory.Companion.DFS
import com.malalisy.algolizer.ui.mstalgorithms.TraversalAlgorithmsContract
import java.lang.IllegalArgumentException

class TraversalAlgorithmsPresenter : TraversalAlgorithmsContract.Presenter {

    companion object {
        private const val VERTEX_ANIMATION_DURATION: Long = 300L
        private const val EDGE_ANIMATION_DURATION: Long = 600L
    }

    private lateinit var orderedVisitedEdges: MutableList<Pair<Int, Int>>
    private lateinit var view: TraversalAlgorithmsContract.View
    private var algorithmType: Int = KRUSKAL_ALGORITHM
    private var algorithm: TraversalAlgorithmRunner? = null

    private var algorithmRun = false
    private lateinit var adjacencyMatrix: Array<Array<Int>>

    private var handler = Handler()

    private var animationIndex = 0

    private var vertexTurn = false

    private val animationRunnable: Runnable = object : Runnable {
        override fun run() {
            val edge = orderedVisitedEdges[animationIndex]
            if (vertexTurn) {
                view.animateVertex(edge.second, VERTEX_ANIMATION_DURATION)
                animationIndex++
                if (animationIndex < orderedVisitedEdges.size) {
                    handler.postDelayed(this, VERTEX_ANIMATION_DURATION)
                } else {
                    handleAnimationEnd()
                }
            } else {
                view.animateEdge(
                    edge.first,
                    edge.second,
                    EDGE_ANIMATION_DURATION * 3 / 4
                )
                handler.postDelayed(this, EDGE_ANIMATION_DURATION)
            }
            vertexTurn = !vertexTurn
        }
    }

    private fun handleAnimationEnd() {
        view.showHideControls(false)
        view.showHideSolution(show = true)

        view.showHideResetButton(true)
        view.showHidePauseButton(false)
        view.showHidePlayButton(true)
    }


    override fun setupView(view: TraversalAlgorithmsContract.View) {
        this.view = view
    }

    override fun onAlgorithmChange(position: Int) {
        algorithmType = when (position) {
            0 -> BFS
            1 -> DFS
            else -> throw IllegalArgumentException()
        }
    }

    override fun onRunClicked(adjacencyList: List<List<Pair<Int, Int>>>) {
        adjacencyMatrix =
            Array(adjacencyList.size) { Array(adjacencyList.size) { 0 } }

        adjacencyList.forEachIndexed { index, list ->
            for (edge in list) {
                adjacencyMatrix[index][edge.first] = edge.second
            }
        }


        algorithm = TraversalAlgorithmsFactory.getAlgorithm(algorithmType, adjacencyMatrix)
        algorithm!!.run()

        view.setAcceptGraphChanges(false)

        this.orderedVisitedEdges = algorithm!!.orderedVisitedEdges

        animationIndex = 0
        vertexTurn = false
        view.animateVertex(orderedVisitedEdges[0].first, VERTEX_ANIMATION_DURATION)
        handler.postDelayed(animationRunnable, VERTEX_ANIMATION_DURATION)

        view.showHidePlayButton(false)
        view.showHidePauseButton(true)
        view.showHideResetButton(false)

    }


    override fun onPauseClicked() {
        handler.removeCallbacks(animationRunnable)
        view.showHidePauseButton(false)
        view.showHidePauseButton(true)
    }

    override fun onRestartClick() {
        view.resetGraph()
    }


    override fun onCloseResultClick() {
        view.setAcceptGraphChanges(true)

        handler.removeCallbacks(animationRunnable)

        view.resetAnimatedGraph()
        animationIndex = 0
        view.showHideSolution(false)
        view.showHideControls(true)
    }
}