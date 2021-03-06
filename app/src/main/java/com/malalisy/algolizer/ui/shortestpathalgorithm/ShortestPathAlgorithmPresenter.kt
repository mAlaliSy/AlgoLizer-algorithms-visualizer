package com.malalisy.algolizer.ui.shortestpathalgorithm

import android.os.Handler
import com.malalisy.algolizer.domain.shortestpath.ShortestPathAlgorithmRunner
import com.malalisy.algolizer.domain.shortestpath.ShortestPathAlgorithmsFactory
import com.malalisy.algolizer.domain.shortestpath.TileType

class ShortestPathAlgorithmPresenter : ShortestPathAlgorithmContract.Presenter {
    companion object {

        const val SOLUTION_BASE_ANIMATION_DURATION = 75L
        const val ALGORITHM_ANIMATION_BASE_TIME = 200L
        const val CHANGE_DESTINATION_LATENCY = 30L
    }

    private var rows = 0
    private var columns = 0
    private var cellSize = 0
    private var algorithmRunner: Int = ShortestPathAlgorithmsFactory.DIJKSTRA_ALGORITHM
    // Store weather the algorithm has run for the current problem
    private var algorithmCompleted = false

    // Visited cells in order of the time they were visited
    private var visitedOrdered: List<Pair<Int, Int>>? = null
    // The grid of the problem
    private lateinit var grid: Array<Array<TileType>>
    // The source node
    private lateinit var source: Pair<Int, Int>
    // The destination node
    private lateinit var destination: Pair<Int, Int>

    private lateinit var newInteractiveDestination: Pair<Int, Int>
    // Shortest path algorithm runner
    private lateinit var shortestPathRunner: ShortestPathAlgorithmRunner

    private lateinit var view: ShortestPathAlgorithmContract.View

    // Determine the current state, weather the start/destination and others has been placed
    private var sourcePlacement = true

    private var destinationPlacement = false
    private var blockPlacement = false
    private var isPlaying = false
    private var touchMovesEnabled = false
    private var interactiveMode = false


    // Algorithm step latency
    private var algorithmStepTime = ALGORITHM_ANIMATION_BASE_TIME
    // Solution animation step latency
    private var solutionStepTime = SOLUTION_BASE_ANIMATION_DURATION

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

    var solution: List<Pair<Int, Int>>? = null
    private var solutionAnimationRunnable: Runnable = object : Runnable {
        override fun run() {
            solution?.let {
                view.animateSolutionCells(
                    it[solutionCellIndex]
                )


                solutionCellIndex++
                if (solutionCellIndex < it.size - 1)
                    handler.postDelayed(this, solutionStepTime)
            }
        }
    }

    val changeDestinationRunnable: Runnable = Runnable {
        changeDestinationInteractively()
    }

    override fun setupView(view: ShortestPathAlgorithmContract.View) {
        this.view = view
        setupDimensions()
        initGrid()

        shortestPathRunner = ShortestPathAlgorithmsFactory.getAlgorithmRunner(
            ShortestPathAlgorithmsFactory.DIJKSTRA_ALGORITHM,
            grid
        )
    }

    fun setupDimensions() {
        val gridDimen = view.getGridDimen()
        cellSize = view.getCellSize()

        rows = gridDimen.y / cellSize
        columns = gridDimen.x / cellSize

        view.setGrid(rows, columns)
    }

    override fun onCellStartTouch(i: Int, j: Int) {
        if (i >= grid.size || j >= grid[0].size) return
        if (interactiveMode) {
            callChangeDestinationInteractively(i, j)
        } else {
            handleGridSelection(i, j)
        }
    }

    /**
     * Handle touching a cell on vertical i and horizontal j
     *
     * @param i
     * @param j
     */
    override fun onCellTouchMove(i: Int, j: Int) {
        if (i >= grid.size || j >= grid[0].size) return
        /**
         * Enable moving event only when placing a block
         */
        if (!touchMovesEnabled) return
        if (interactiveMode) {
            callChangeDestinationInteractively(i, j)
        } else {
            handleGridSelection(i, j)
        }
    }

    private fun callChangeDestinationInteractively(i: Int, j: Int) {
        if ((grid[i][j] == TileType.Source || grid[i][j] == TileType.Block)
            || i == destination.first && j == destination.second
        ) return

        newInteractiveDestination = i to j
        handler.removeCallbacks(changeDestinationRunnable)
        handler.postDelayed(changeDestinationRunnable, CHANGE_DESTINATION_LATENCY)
    }

    private fun changeDestinationInteractively() {
        val i = newInteractiveDestination.first
        val j = newInteractiveDestination.second

        view.animateRemoveDestinationCell(destination)
        destination = newInteractiveDestination

        val animatedOldSolutionCells = solution?.subList(0, solutionCellIndex)
        val animatedOldVisitedCells = visitedOrdered?.subList(0, visitedIndex)

        shortestPathRunner.run(source, destination)
        visitedOrdered = shortestPathRunner.orderedVisitedCells
        solution = shortestPathRunner.solution
        view.showHideResultContainer(
            true,
            shortestPathRunner.destinationReached,
            shortestPathRunner.solutionCost
        )
        view.showHideControls(false)

        val visitedToBeAnimated = mutableSetOf<Pair<Int, Int>>()
        val visitedToBeRemoved = mutableListOf<Pair<Int, Int>>()
        // If the animation of the previous problem is not started
        if (animatedOldVisitedCells == null) {
            visitedToBeAnimated += visitedOrdered!!
        } else {
            // Remove all visited elements in previous problem that is not in the current problem
            animatedOldVisitedCells.forEach {
                if (!visitedOrdered!!.contains(it)) visitedToBeRemoved += it
            }
            // Add visited cells that are in the current problem but they aren't in the previous problem
            visitedOrdered?.forEach {
                if (it !in animatedOldVisitedCells) {
                    visitedToBeAnimated.add(it)
                }
            }


        }
        visitedIndex = visitedOrdered!!.size

        view.animateVisitedItems(*visitedToBeAnimated.toTypedArray())
        view.animateRemoveVisitedItems(*visitedToBeRemoved.toTypedArray())

        if (solution == null) {
            if (animatedOldSolutionCells != null)
                view.animateRemoveSolutionCells(*animatedOldSolutionCells.toTypedArray())
            return
        }
        val solToBeAnimated = mutableSetOf<Pair<Int, Int>>()
        val solToBeRemoved = mutableListOf<Pair<Int, Int>>()
        // If the animation of the previous problem is not started
        if (animatedOldSolutionCells == null) {
            solToBeAnimated += solution!!
        } else {
            // Remove all visited elements in previous problem that is not in the current problem
            animatedOldSolutionCells.forEach {
                if (!solution!!.contains(it)) solToBeRemoved += it
            }
            // Add visited cells that are in the current problem but they aren't in the previous problem
            solution?.forEach {
                if (it !in animatedOldSolutionCells) {
                    solToBeAnimated.add(it)
                }
            }


        }
        solToBeAnimated.remove(solution!![0])
        solToBeAnimated.remove(solution!![solution!!.size - 1])
        solutionCellIndex = solution!!.size

        view.animateSolutionCells(*solToBeAnimated.toTypedArray())
        view.animateRemoveSolutionCells(*solToBeRemoved.toTypedArray())

        // Animate the destination after the animation of visited items removal
        view.animateDestinationItem(i, j)

        handler.postDelayed({
        }, 500)
    }

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
            }
            destinationPlacement -> {
                grid[i][j] = TileType.Destination
                destination = i to j
                destinationPlacement = false
                blockPlacement = true
                view.showHideInteractiveModeButton(true)
                view.showHideDestinationLabel(false)
                view.showHideControls(true)
                view.animateDestinationItem(i, j)
            }
            else -> {
                touchMovesEnabled = true
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
        shortestPathRunner.run(source, destination)
        this.solution = shortestPathRunner.solution
        algorithmCompleted = true
        visitedOrdered = shortestPathRunner.orderedVisitedCells
        view.setAnimationSeekBarMaxValue(visitedOrdered!!.size)
        view.showHideAnimationSeekBar(true)
    }

    /**
     * Handle reaching the end of visited cells animation,
     * if a solution was found, it tells the view to animate the solution cells and displays its cost
     * if no solution was found, it tells the view to display a proper message
     *
     */
    private fun handleAlgorithmAnimationEnd() {
        if (solution != null) {
            solutionCellIndex = 1
            view.showHideControls(false)
            view.showHideResultContainer(true, true, shortestPathRunner.solutionCost)
            handler.postDelayed(solutionAnimationRunnable, SOLUTION_BASE_ANIMATION_DURATION)
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
        while (visitedIndex < newIndex && visitedIndex < visitedOrdered!!.size) {
            cells.add(visitedOrdered!![visitedIndex])
            visitedIndex++
        }

        view.animateVisitedItems(*cells.toTypedArray())
        view.setAnimationSeekBarValue(visitedIndex)

        // Check if we reached to the end of visited cells, if so, display the result
        if (visitedIndex >= visitedOrdered!!.size) {
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
        if (visitedIndex < 0 || visitedIndex >= visitedOrdered!!.size) return
        val cells = mutableListOf<Pair<Int, Int>>()
        while (visitedIndex >= newIndex && visitedIndex < visitedOrdered!!.size) {
            cells.add(visitedOrdered!![visitedIndex])
            visitedIndex--
        }
        if (visitedIndex == -1) visitedIndex = 0

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
     * On reset button on bottom sheet view clicked
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
        initGrid()
        view.clearGrid()
        view.showHidePlayButton(true)
        view.showHidePauseButton(false)
        view.showHideAnimationSeekBar(false)
        view.setAnimationSeekBarValue(0)
        view.showHideInteractiveModeButton(false)
        view.showHideControls(false, false)
        view.showHideSourceLabel(true)
        view.setInteractiveMode(false)
        visitedOrdered = arrayListOf()
        visitedIndex = 0
        interactiveMode = false
        touchMovesEnabled = false
        shortestPathRunner = ShortestPathAlgorithmsFactory.getAlgorithmRunner(
            algorithmRunner,
            grid
        )

    }

    private fun initGrid() {
        grid = Array(rows) {
            Array(columns) {
                TileType.Empty
            }
        }
    }

    override fun onInteractiveCheckChange(enabled: Boolean) {
        interactiveMode = enabled
        touchMovesEnabled = true
    }

    override fun onAlgorithmSelected(position: Int) {

        val nalgorithmRunner = when (position) {
            0 -> ShortestPathAlgorithmsFactory.DIJKSTRA_ALGORITHM
            1 -> ShortestPathAlgorithmsFactory.AStar_ALGORITHM
            2 -> ShortestPathAlgorithmsFactory.GREEDY_BEST_FIRST_ALGORITHM
            3 -> ShortestPathAlgorithmsFactory.BFS_ALGORITHM
            else -> throw IllegalArgumentException("Unsupported Algorithm")
        }
        if (nalgorithmRunner != algorithmRunner) {
            algorithmRunner = nalgorithmRunner
            refreshAlgorithmRunner()
        }
    }

    private fun refreshAlgorithmRunner() {
        shortestPathRunner = ShortestPathAlgorithmsFactory.getAlgorithmRunner(
            algorithmRunner,
            grid
        )
        shortestPathRunner.run(source, destination)

        solution?.let {
            val cells = it.subList(0, solutionCellIndex).toTypedArray()
            view.animateRemoveSolutionCells(*cells)
        }
        visitedOrdered?.let {
            val cells = it.subList(0, visitedIndex).toTypedArray()
            view.animateRemoveVisitedItems(*cells)
        }
        view.setAnimationSeekBarValue(0)

        visitedOrdered = shortestPathRunner.orderedVisitedCells
        solution = shortestPathRunner.solution
        visitedIndex = 0
        solutionCellIndex = 0
        algorithmCompleted = true
        view.showHideAnimationSeekBar(true)
        view.setAnimationSeekBarMaxValue(visitedOrdered!!.size)
    }

    override fun onCloseResultClick() {
        handler.removeCallbacks(solutionAnimationRunnable)
        view.setAnimationSeekBarValue(0)
        solution?.let {
            val cells = it.subList(0, solutionCellIndex).toTypedArray()
            view.animateRemoveSolutionCells(*cells)
        }
        visitedOrdered?.let {
            val cells = it.subList(0, visitedIndex).toTypedArray()
            view.animateRemoveVisitedItems(*cells)
        }
        visitedIndex = 0
        solutionCellIndex = 0
    }

}
