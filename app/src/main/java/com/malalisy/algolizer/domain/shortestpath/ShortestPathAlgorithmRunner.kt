package com.malalisy.algolizer.domain.shortestpath

import com.malalisy.algolizer.domain.AlgorithmRunner
import java.util.*

/**
 * An abstract class that has shared functionality between shortest path algorithms
 *
 */
abstract class ShortestPathAlgorithmRunner : AlgorithmRunner() {
    /**
     * A flag for algorithm completion, if the algorithm has completed this does not mean that it
     * found a path from the source to the destination
     */
    var isDone: Boolean = false
    /**
     * A flag that states wheather the algorithm could found a path from the source to the
     * destination.
     */
    var destinationReached = false
    /**
     * A grid that represent the graph
     */
    lateinit var grid: Array<Array<TileType>>

    /**
     * A matrix for flagging the visited cells
     */
    lateinit var visitedCells: Array<Array<Boolean>>

    /**
     * A list that contains the path from source to destination
     */
    lateinit var solution: List<Pair<Int, Int>>

    /**
     * The total cost from source to destination
     */
    var solutionCost: Int = -1

    /**
     * Setup the algorithm with a problem (a grid that contains the source and the destination and
     * may be some obstacles)
     *
     * @param grid
     */
    protected open fun setup(grid: Array<Array<TileType>>) {
        this.grid = grid
        isDone = false
        destinationReached = false
        visitedCells = Array(grid.size) {
            Array(grid[0].size) {
                false
            }
        }
    }


    /**
     * Move one step forward in the execution of the algorithm
     *
     * @return the new
     */
    abstract fun moveForward(): Pair<Int, Int>?

    /**
     * Move the algorithm execution forward with the number of steps as passed
     *
     * @param steps The number of execution steps to move.
     * @return The newly visited cells during the moves
     */
    fun moveForward(steps: Int): Array<Pair<Int, Int>?> {
        return Array(steps) {
            moveForward()
        }
    }

    /**
     * Find the source node
     *
     * @return the source node position, if no source node it return Pair(-1, -1)
     */
    fun findSource(): Pair<Int, Int> {
        for (i in grid.indices) {
            for (j in grid[0].indices) {
                if (grid[i][j] == TileType.Source) {
                    return i to j
                }
            }
        }

        return -1 to -1
    }

    override fun run() {
        while (!isDone) {
            moveForward()
        }
    }

    /**
     * Find the path by going backward using the parent
     *
     * @param node the node of destination
     */
    fun findPath(node: ShortestPathNode) {
        val sol = mutableListOf<Pair<Int, Int>>()
        var temp = node

        val reversedSol: Stack<Pair<Int, Int>> = Stack()
        while (temp.parent != null) {
            reversedSol.push(temp.position)
            temp = temp.parent!!
        }
        reversedSol.push(temp.position)

        while (reversedSol.isNotEmpty()) {
            sol.add(reversedSol.pop())
        }

        this.solution = sol
        this.solutionCost = node.distance
    }

}