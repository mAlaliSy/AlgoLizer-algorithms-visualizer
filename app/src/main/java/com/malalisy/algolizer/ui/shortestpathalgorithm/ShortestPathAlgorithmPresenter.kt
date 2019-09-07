package com.malalisy.algolizer.ui.shortestpathalgorithm

import android.os.Handler
import com.malalisy.algolizer.domain.shortestpath.BfsAlgorithmRunner
import com.malalisy.algolizer.domain.shortestpath.ShortestPathAlgorithmRunner
import com.malalisy.algolizer.domain.shortestpath.TileType

class ShortestPathAlgorithmPresenter : ShortestPathAlgorithmContract.Presenter {
    private var isDone = false
    private val grid = Array(10) {
        Array(5) {
            TileType.Empty
        }
    }
    private lateinit var source: Pair<Int, Int>
    private lateinit var shortestPatRunner: ShortestPathAlgorithmRunner

    private lateinit var view: ShortestPathAlgorithmContract.View

    private var sourcePlacement = false

    private var destinationPlacement = false

    private var blockPlacement = false

    private var isPlaying = false

    private var algorithmRunningSpeed = 300L

    var handler = Handler()

    private var moveForwardRunnable: Runnable = object : Runnable {
        override fun run() {
            moveForward()
            if (isPlaying)
                handler.postDelayed(this, algorithmRunningSpeed)
        }
    }

    override fun setupView(view: ShortestPathAlgorithmContract.View) {
        this.view = view
        sourcePlacement = true
    }

    override fun onItemSelected(i: Int, j: Int) {
        if (i >= grid.size || j >= grid[0].size) return
        if (grid[i][j] != TileType.Empty) return

        when {
            sourcePlacement -> {
                grid[i][j] = TileType.Source
                source = i to j
                sourcePlacement = false
                destinationPlacement = true
                view.hideSourceLabel()
                view.showDestinationLabel()

                view.animateSourceItem(i, j)
                shortestPatRunner = BfsAlgorithmRunner(grid, source)
            }
            destinationPlacement -> {
                grid[i][j] = TileType.Destination
                destinationPlacement = false
                blockPlacement = true
                view.hideDestinationLabel()
                view.showControls()
                view.animateDestinationItem(i, j)
            }
            else -> {
                grid[i][j] = TileType.Block
                view.animateBlockItem(i, j)
            }
        }
    }

    override fun onPlayClicked() {
        if (isPlaying) {
            isPlaying = false
        } else {
            isPlaying = true
            handler.postDelayed(moveForwardRunnable, algorithmRunningSpeed)
        }
    }

    private fun moveForward() {
        if (isDone) return
        val cell = shortestPatRunner.moveForward()
        if(cell == null){
            isDone = true
            return
        }
        if (grid[cell.first][cell.second] == TileType.Empty)
            view.animateVisitedItems(cell)
        checkDone()
    }

    private fun checkDone() {
        if (shortestPatRunner.isDone) {
            isDone = true
            isPlaying = false
            handler.removeCallbacks(moveForwardRunnable)

            if (shortestPatRunner.destinationReached) {
                view.showSolution(shortestPatRunner.solution)
            } else {
                view.showNoPathFound()
            }
        }
    }

    override fun onForwardClicked() {
        moveForward()
    }

}