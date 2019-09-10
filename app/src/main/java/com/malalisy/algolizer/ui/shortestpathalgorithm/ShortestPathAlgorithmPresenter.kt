package com.malalisy.algolizer.ui.shortestpathalgorithm

import android.os.Handler
import com.malalisy.algolizer.domain.shortestpath.BfsAlgorithmRunner
import com.malalisy.algolizer.domain.shortestpath.ShortestPathAlgorithmRunner
import com.malalisy.algolizer.domain.shortestpath.TileType
import java.util.ArrayList

class ShortestPathAlgorithmPresenter : ShortestPathAlgorithmContract.Presenter {
    companion object {

        const val SOLUTION_ANIMATION_DURATION = 150L

        const val ALGORITHM_ANIMATION_BASE_SPEED = 200L
    }

    private var algorithmStarted = false

    private var isAlgorithmRunFinished = false

    private lateinit var visitedOrdered: ArrayList<Pair<Int, Int>>
    private var grid = initGrid()

    private lateinit var source: Pair<Int, Int>

    private lateinit var shortestPatRunner: ShortestPathAlgorithmRunner


    private lateinit var view: ShortestPathAlgorithmContract.View
    private var sourcePlacement = true
    private var destinationPlacement = false
    private var blockPlacement = false

    private var isPlaying = false

    private var algorithmRunningSpeed = ALGORITHM_ANIMATION_BASE_SPEED
    var handler = Handler()

    private var visitedIndex = 0
    private var moveForwardRunnable: Runnable = object : Runnable {
        override fun run() {
            moveForward()
            if (visitedIndex == visitedOrdered.size) {
                isPlaying = false
                handleAlgorithmAnimationEnd()
                return
            }
            if (isPlaying)
                handler.postDelayed(this, algorithmRunningSpeed)
        }
    }

    var solutionCellIndex = 0
    lateinit var solution: List<Pair<Int, Int>>
    private var solutionAnimationRunnable: Runnable = object : Runnable {
        override fun run() {
            view.animateSolutionCell(
                solution[solutionCellIndex].first,
                solution[solutionCellIndex].second
            )
            solutionCellIndex++
            if (solutionCellIndex < solution.size - 2)
                handler.postDelayed(this, SOLUTION_ANIMATION_DURATION)
        }
    }

    override fun setupView(view: ShortestPathAlgorithmContract.View) {
        this.view = view
    }

    // To avoid placing blocks when touching a destination cell and moving
    private var blocksPlacementStarted = false

    override fun onCellStartTouch(i: Int, j: Int) {
        handleGridSelection(i, j)
    }

    override fun onCellTouchMove(i: Int, j: Int) {
        /**
         * Enable moving event only when placing a block
         */
        if (!blocksPlacementStarted) return
        handleGridSelection(i, j)
    }

    private fun handleGridSelection(i: Int, j: Int) {
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
                blocksPlacementStarted = true
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
        if (!isAlgorithmRunFinished) {
            runAlgorithm()
        }
        handler.postDelayed(moveForwardRunnable, algorithmRunningSpeed)
    }

    private fun runAlgorithm() {
        shortestPatRunner.run()
        isAlgorithmRunFinished = true
        visitedOrdered = shortestPatRunner.orderedVisitedCells
        view.setAnimationSeekBarMaxValue(visitedOrdered.size)
        view.showHideAnimationSeekBar(true)
    }

    private fun handleAlgorithmAnimationEnd() {
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

    override fun onForwardClicked() {
        moveForward()
    }

    private fun moveForward() {
        visitedIndex++
        if (visitedIndex >= visitedOrdered.size) {
            handleAlgorithmAnimationEnd()
            return
        }
        view.animateVisitedItems(visitedOrdered[visitedIndex])
        view.setAnimationSeekBarValue(visitedIndex)
    }

    private fun moveBackward() {
        if (visitedIndex < 0 || visitedIndex >= visitedOrdered.size) return
        view.animateRemoveVisitedItems(
            visitedOrdered[visitedIndex].first,
            visitedOrdered[visitedIndex].second
        )
        view.setAnimationSeekBarValue(visitedIndex)
        visitedIndex--
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


    override fun onAnimationSeekBarChanged(value: Int) {
        val animateTo: Int = when (value) {
            0 -> 0
            visitedOrdered.size -> value - 1
            else -> value
        }
        if (animateTo > visitedIndex)
            while (visitedIndex < animateTo) moveForward()
        else if (value < visitedIndex)
            while (visitedIndex > animateTo) moveBackward()
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
        view.showHideAnimationSeekBar(false)
        view.setAnimationSeekBarValue(0)

        isAlgorithmRunFinished = false
        visitedOrdered = arrayListOf()
        visitedIndex = 0

        blocksPlacementStarted = false
    }

    private fun initGrid(): Array<Array<TileType>> =
        Array(24) {
            Array(15) {
                TileType.Empty
            }
        }
}
