package com.malalisy.algolizer.domain.shortestpath.algorithmsimp

import com.malalisy.algolizer.domain.shortestpath.ShortestPathAlgorithmRunner
import com.malalisy.algolizer.domain.shortestpath.ShortestPathNode
import com.malalisy.algolizer.domain.shortestpath.TileType
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
class BfsAlgorithmRunner(grid: Array<Array<TileType>>) :
    ShortestPathAlgorithmRunner(grid) {

    lateinit var queue: Queue<ShortestPathNode>

    init {
        setup(grid)
    }

    override fun run(source: Pair<Int, Int>, destination: Pair<Int, Int>) {
        queue = LinkedList()
        for (i in visitedCells.indices)
            for (j in visitedCells[i].indices)
                visitedCells[i][j] = false
        orderedVisitedCells = mutableListOf()
        destinationReached=false
        queue.add(
            ShortestPathNode(
                source.copy(),
                0,
                null
            )
        )
        visitedCells[source.first][source.second] = true

        var node: ShortestPathNode?
        do {
            node = queue.poll()
            if (node == null) break
            for (h in horizontalDir.indices) {
                val i = node.position.first + verticalDir[h]
                val j = node.position.second + horizontalDir[h]
                if (i >= grid.size || i < 0 || j >= grid[i].size || j < 0) continue

                val tile = grid[i][j]

                if (tile != TileType.Block && !visitedCells[i][j]) {
                    visitedCells[i][j] = true
                    val newNode = ShortestPathNode(
                        i to j,
                        node.distance + 1,
                        node
                    )
                    if (newNode.position == destination) {
                        destinationReached = true
                        findSolution(newNode)
                        orderedVisitedCells.add(node.position)
                        return
                    } else {
                        queue.add(newNode)
                    }
                }
            }

            if (node.position != source)
                orderedVisitedCells.add(node.position)
        } while (true)
    }

}