package com.malalisy.algolizer.ui.shortestpathalgorithm

import android.os.Handler
import com.malalisy.algolizer.domain.shortestpath.BfsAlgorithmRunner
import com.malalisy.algolizer.domain.shortestpath.ShortestPathAlgorithmRunner
import com.malalisy.algolizer.domain.shortestpath.TileType
import java.util.ArrayList

class ShortestPathAlgorithmPresenter : ShortestPathAlgorithmContract.Presenter {
    companion object {
        const val SOLUTION_ANIMATION_DURATION = 150L
        const val ALGORITHM_ANIMATION_BASE_TIME = 200L
    }

    // Store weather the algorithm has run for the current problem
    private var algorithmCompleted = false
    // Visited cells in order of the time they were visited
    private lateinit var visitedOrdered: List<Pair<Int, Int>>
    // The grid of the problem
    private var grid = initGrid()
    // The source node
    private lateinit var source: Pair<Int, Int>
    // Shortest path algorithm runner
    private lateinit var shortestPathRunner: ShortestPathAlgorithmRunner

    private lateinit var view: ShortestPathAlgorithmContract.View

    // Determine the current state, weather the start/destination and others has been placed
    private var sourcePlacement = true
    private var destinationPlacement = false
    private var blockPlacement = false

    private var isPlaying = false

    // Algorithm step latency
    private var algorithmStepTime = ALGORITHM_ANIMATION_BASE_TIME
    var handler = Handler()

    private var visitedIndex = 0
    private var moveForwardRunnable: Runnable = object : Runnable {
        override fun run() {
            moveForward(visitedIndex + 1)
            if (isPlaying)
                handler.postDelayed(this, algorithmStepTime)
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
            if (solutionCellIndex < solution.size - 1)
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

    /**
     * Handle touching a cell on vertical i and horizontal j
     *
     * @param i
     * @param j
     */
    private fun handleGridSelection(i: Int, j: Int) {
        if (algorithmCompleted) return
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
                shortestPathRunner = BfsAlgorithmRunner(grid, source)
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
        view.showHidePauseButton(true)
        view.showHidePlayButton(false)
        if (isPlaying) return
        isPlaying = true
        /**
         * Check if the algorithm has been run on the current grid and the solution was found
         */
        if (!algorithmCompleted) {
            runAlgorithm()
        }
        // Play/Resume the algorithm animation on visited cells
        handler.postDelayed(moveForwardRunnable, algorithmStepTime)
    }

    /**
     * Run the algorithm on the current grid,
     * get the visited cells in order of time they where visited
     *
     */
    private fun runAlgorithm() {
        shortestPathRunner.run()
        algorithmCompleted = true
        visitedOrdered = shortestPathRunner.orderedVisitedCells
        view.setAnimationSeekBarMaxValue(visitedOrdered.size)
        view.showHideAnimationSeekBar(true)
    }

    /**
     * Handle reaching the end of visited cells animation,
     * if a solution was found, it tells the view to animate the solution cells and displays its cost
     * if no solution was found, it tells the view to display a proper message
     *
     */
    private fun handleAlgorithmAnimationEnd() {
        if (shortestPathRunner.destinationReached) {
            this.solution = shortestPathRunner.solution
            solutionCellIndex = 1
            view.showHideControls(false)
            view.showHideResultContainer(true, true, shortestPathRunner.solutionCost)
            handler.postDelayed(solutionAnimationRunnable, SOLUTION_ANIMATION_DURATION)
        } else {
            view.showHideControls(false)
            view.showHideResultContainer(true, false)
        }
    }

    override fun onForwardClicked() {
        if (!algorithmCompleted)
            runAlgorithm()
        moveForward(visitedIndex + 1)
    }

    /**
     * Move a single step forward on the algorithm execution by animating the next visited cell
     *
     */
    private fun moveForward(newIndex: Int) {
        val cells = mutableListOf<Pair<Int, Int>>()
        while (visitedIndex < newIndex && visitedIndex < visitedOrdered.size) {
            cells.add(visitedOrdered[visitedIndex])
            visitedIndex++
        }

        view.animateVisitedItems(*cells.toTypedArray())
        view.setAnimationSeekBarValue(visitedIndex)

        // Check if we reached to the end of visited cells, if so, display the result
        if (visitedIndex >= visitedOrdered.size) {
            isPlaying = false
            handleAlgorithmAnimationEnd()
            return
        }
    }


    /**
     * Move a single step backward on the algorithm execution by animating the removing the last
     * animated visited cell
     *
     */
    private fun moveBackward(newIndex: Int) {
        if (visitedIndex < 0 || visitedIndex >= visitedOrdered.size) return
        val cells = mutableListOf<Pair<Int, Int>>()
        while (visitedIndex >= newIndex && visitedIndex < visitedOrdered.size) {
            cells.add(visitedOrdered[visitedIndex])
            visitedIndex--
        }
        if(visitedIndex == -1) visitedIndex = 0

        view.animateRemoveVisitedItems(*cells.toTypedArray())
        view.setAnimationSeekBarValue(visitedIndex)
    }

    override fun onPauseClicked() {
        pause()
    }

    /**
     * Pause the execution of animating visited cells
     *
     */
    private fun pause() {
        view.showHidePauseButton(false)
        view.showHidePlayButton(true)
        if (!isPlaying) return
        isPlaying = false
        handler.removeCallbacks(moveForwardRunnable)
    }

    /**
     * On fragment pause
     *
     */
    override fun onViewPause() {
        pause()
        handler.removeCallbacks(solutionAnimationRunnable)
    }

    /**
     * On algorithm animation speed changed
     *
     * @param speed the speed factor
     */
    override fun onSpeedChanged(speed: Float) {
        algorithmStepTime = (1.0f / speed * ALGORITHM_ANIMATION_BASE_TIME).toLong()
    }


    /**
     * On seek bar changed by the user, move the animation execution forward/backward to sync with
     * the value of the seek bar
     *
     * @param value value of the seek bar from 0 to number of visited cells
     */
    override fun onAnimationSeekBarChanged(value: Int) {
        if (value > visitedIndex)
            moveForward(value)
        else if (value < visitedIndex)
            moveBackward(value)
    }


    /**
     * On restart button on the result view clicked
     *
     */
    override fun onRestartClick() {
        reset()
    }

    /**
     * Reset all the data to handle a new problem
     *
     */
    private fun reset() {
        view.showHideSourceLabel(true)
        view.showHideResultContainer(false)
        sourcePlacement = true
        algorithmCompleted = false
        grid = initGrid()
        view.clearGrid()
        view.showHidePlayButton(true)
        view.showHidePauseButton(false)
        view.showHideAnimationSeekBar(false)
        view.setAnimationSeekBarValue(0)
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
