package com.malalisy.algolizer.ui.shortestpathalgorithm

import android.os.Handler
import com.malalisy.algolizer.domain.shortestpath.BfsAlgorithmRunner
import com.malalisy.algolizer.domain.shortestpath.ShortestPathAlgorithmRunner
import com.malalisy.algolizer.domain.shortestpath.TileType

class ShortestPathAlgorithmPresenter : ShortestPathAlgorithmContract.Presenter {


    companion object {
        const val SOLUTION_ANIMATION_DURATION = 100L
        const val ALGORITHM_ANIMATION_BASE_SPEED = 200L
    }

    private var algorithmStarted = false

    private var isDone = false
    private val grid = Array(18) {
        Array(10) {
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

    private var algorithmRunningSpeed = ALGORITHM_ANIMATION_BASE_SPEED

    var handler = Handler()


    private var moveForwardRunnable: Runnable = object : Runnable {
        override fun run() {
            moveForward()
            if (isPlaying)
                handler.postDelayed(this, algorithmRunningSpeed)
        }
    }

    lateinit var solution: List<Pair<Int, Int>>

    var solutionCellIndex = 0

    private var solutionAnimationRunnable: Runnable = object : Runnable {
        override fun run() {
            view.animateSolutionCell(
                solution[solutionCellIndex].first,
                solution[solutionCellIndex].second
            )
            solutionCellIndex++
            if (solutionCellIndex < solution.size - 1)
                handler.postDelayed(this, SOLUTION_ANIMATION_DURATION)
        }
    }

    override fun setupView(view: ShortestPathAlgorithmContract.View) {
        this.view = view
        sourcePlacement = true
    }

    override fun onItemSelected(i: Int, j: Int) {
        if (algorithmStarted) return
        if (i >= grid.size || i < 0 || j >= grid[0].size || j < 0) return
        if (grid[i][j] != TileType.Empty) return

        when {
            sourcePlacement -> {
                grid[i][j] = TileType.Source
                source = i to j
                sourcePlacement = false
                destinationPlacement = true
                view.hideSourceLabel()
                view.showHideDestinationLabel(true)

                view.animateSourceItem(i, j)
                shortestPatRunner = BfsAlgorithmRunner(grid, source)
            }
            destinationPlacement -> {
                grid[i][j] = TileType.Destination
                destinationPlacement = false
                blockPlacement = true
                view.showHideDestinationLabel(false)
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
        algorithmStarted = true
        view.showHidePauseButton(true)
        view.showHidePlayButton(false)
        if (isPlaying) return
        isPlaying = true
        handler.postDelayed(moveForwardRunnable, algorithmRunningSpeed)
    }

    private fun moveForward() {
        if (isDone) return
        val cell = shortestPatRunner.moveForward()
        if (cell == null) {
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
                this.solution = shortestPatRunner.solution
                solutionCellIndex = 1
                // TODO: Hide controls and show the solution cost in a solution label
                handler.postDelayed(solutionAnimationRunnable, SOLUTION_ANIMATION_DURATION)
            } else {
                view.showNoPathFound()
            }
        }
    }

    override fun onForwardClicked() {
        moveForward()
    }

    override fun onPauseClicked() {
        pause()
    }

    private fun pause() {
        view.showHidePauseButton(false)
        view.showHidePlayButton(true)
        if (!isPlaying) return
        isPlaying = false
        handler.removeCallbacks(moveForwardRunnable)
    }

    override fun onViewPause() {
        pause()
        handler.removeCallbacks(solutionAnimationRunnable)
    }

    override fun onSpeedChanged(speed: Float) {
        algorithmRunningSpeed = (1.0f / speed * ALGORITHM_ANIMATION_BASE_SPEED).toLong()
    }
}
