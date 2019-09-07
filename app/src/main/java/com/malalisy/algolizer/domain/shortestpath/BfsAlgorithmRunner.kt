package com.malalisy.algolizer.domain.shortestpath

import com.malalisy.algolizer.utils.horizontalDir
import com.malalisy.algolizer.utils.verticalDir
import java.util.*

/**
 * A class that implement the BFS algorithm for finding the shortest path in an unweighted graph
 *
 * @constructor
 *
 *
 * @param grid
 * @param source
 */
class BfsAlgorithmRunner(grid: Array<Array<TileType>>, source: Pair<Int, Int>) :
    SingleSourceShortestPathAlgorithmRunner(grid, source) {

    lateinit var queue: Queue<ShortestPathNode>

    init {
        setup(grid, source)
    }

    override fun setup(grid: Array<Array<TileType>>, source: Pair<Int, Int>) {
        super.setup(grid)

        queue = LinkedList()
        queue.add(ShortestPathNode(source.copy(), 0, null))
    }


    override fun moveForward(): Pair<Int, Int>? {
        val node = queue.poll()
        if (node == null){
            isDone = true
            return null
        }
        for (h in horizontalDir.indices) {
            val i = node.position.first + verticalDir[h]
            val j = node.position.second + horizontalDir[h]
            if (i >= grid.size || i < 0 || j >= grid[i].size || j < 0) continue

            val tile = grid[i][j]

            if (tile != TileType.Block && !visitedCells[i][j]) {
                visitedCells[i][j] = true
                val newNode = ShortestPathNode(i to j, node.distance + 1, node)
                if (tile == TileType.Destination) {
                    isDone = true
                    destinationReached = true

                    findPath(newNode)
                } else {
                    queue.add(newNode)
                }
            }
        }

        return node.position
    }

}