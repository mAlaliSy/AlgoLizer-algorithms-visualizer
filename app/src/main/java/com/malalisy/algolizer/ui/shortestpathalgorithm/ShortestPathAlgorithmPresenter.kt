package com.malalisy.algolizer.ui.shortestpathalgorithm

import android.os.Handler
import android.util.Log
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

    private var grid = initGrid()

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
                view.showHideSourceLabel(false)
                view.showHideDestinationLabel(true)

                view.animateSourceItem(i, j)
                shortestPatRunner = BfsAlgorithmRunner(grid, source)
            }
            destinationPlacement -> {
                grid[i][j] = TileType.Destination
                destinationPlacement = false
                blockPlacement = true
                view.showHideDestinationLabel(false)
                view.showHideControls(true)
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
        val cell = shortestPatRunner.moveForward()
        checkDone()
        if (cell == null) return
        if (grid[cell.first][cell.second] == TileType.Empty)
            view.animateVisitedItems(cell)
    }

    private fun checkDone() {
        if (shortestPatRunner.isDone) {
            isDone = true
            isPlaying = false
            handler.removeCallbacks(moveForwardRunnable)

            if (shortestPatRunner.destinationReached) {
                this.solution = shortestPatRunner.solution
                solutionCellIndex = 1
                view.showHideControls(false)
                view.showHideResultContainer(true, true, shortestPatRunner.solutionCost)
                handler.postDelayed(solutionAnimationRunnable, SOLUTION_ANIMATION_DURATION)
            } else {
                view.showHideControls(false)
                view.showHideResultContainer(true, false)
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

    override fun onRestartClick() {
        reset()
    }

    private fun reset() {
        view.showHideSourceLabel(true)
        view.showHideResultContainer(false)
        sourcePlacement = true
        algorithmStarted = false
        grid = initGrid()
        view.clearGrid()
        view.showHidePlayButton(true)
        view.showHidePauseButton(false)
    }

    private fun initGrid(): Array<Array<TileType>> =
        Array(18) {
            Array(10) {
                TileType.Empty
            }
        }
}
