package com.malalisy.algolizer.domain.shortestpath

import java.util.*
import kotlin.collections.ArrayList

/**
 * An abstract class that has shared functionality between shortest path algorithms
 *
 */
abstract class ShortestPathAlgorithmRunner(grid:Array<Array<TileType>>) {
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
    var solution: List<Pair<Int, Int>>? = null

    /**
     * The total cost from source to destination
     */
    var solutionCost: Int = -1

    /**
     * The visited cells in order by time they have been visited
     */
    var orderedVisitedCells:MutableList<Pair<Int, Int>> = ArrayList()


    init {
        this.setup(grid)
    }


    /**
     * Setup the algorithm with a problem (a grid that contains the source and the destination and
     * may be some obstacles)
     *
     * @param grid
     */
    protected open fun setup(grid: Array<Array<TileType>>) {
        this.grid = grid
        destinationReached = false
        visitedCells = Array(grid.size) {
            Array(grid[0].size) {
                false
            }
        }
    }

    abstract fun run(source:Pair<Int, Int>, destination:Pair<Int, Int>)

    /**
     * Find the solution path by going backward using the parent
     *
     * @param node the node of destination
     */
    fun findSolution(node: ShortestPathNode) {
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