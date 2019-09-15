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

    override fun run(source: Pair<Int, Int>, destination: Pair<Int, Int>) {
        queue = LinkedList()
        clearVisited()
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
            for (neighbor in findNeighbors(node.position)){
                val tile = grid[neighbor.first][neighbor.second]

                if (tile != TileType.Block && !visitedCells[neighbor.first][neighbor.second]) {
                    visitedCells[neighbor.first][neighbor.second] = true
                    val newNode = ShortestPathNode(
                        neighbor,
                        node.cost + 1,
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