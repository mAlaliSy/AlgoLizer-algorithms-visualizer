package com.malalisy.algolizer.domain.shortestpath.algorithmsimp

import com.malalisy.algolizer.domain.shortestpath.ShortestPathAlgorithmRunner
import com.malalisy.algolizer.domain.shortestpath.ShortestPathNode
import com.malalisy.algolizer.domain.shortestpath.TileType
import java.util.*
import kotlin.math.abs

class AStarAlgorithmRunner(grid: Array<Array<TileType>>) : ShortestPathAlgorithmRunner(grid) {

    override fun run(source: Pair<Int, Int>, destination: Pair<Int, Int>) {
        val costs = Array(grid.size) {
            Array(grid[it].size) { pos -> Int.MAX_VALUE }
        }
        costs[source.first][source.second] = 0
        val queue = PriorityQueue<ShortestPathNode>(20) { a, b ->
            a.cost - b.cost
        }
        queue.add(ShortestPathNode(source, 0, null))

        orderedVisitedCells = mutableListOf()
        destinationReached=false

        while (queue.isNotEmpty()) {
            val node = queue.poll()
            if (node.position == destination) {
                destinationReached = true
                findSolution(node)
                break
            }

            for (neighbor in findNeighbors(node.position)) {
                if (grid[neighbor.first][neighbor.second] == TileType.Block) continue
                val ncost = costs[node.position.first][node.position.second] + 1
                if (ncost < costs[neighbor.first][neighbor.second]) {
                    costs[neighbor.first][neighbor.second] =
                        costs[node.position.first][node.position.second] + 1
                    queue.add(
                        ShortestPathNode(
                            neighbor,
                            ncost + heuristic(neighbor, destination),
                            node
                        )
                    )

                    if (neighbor != destination)
                        orderedVisitedCells.add(neighbor)
                }
            }


        }

    }

    private fun heuristic(point: Pair<Int, Int>, destination: Pair<Int, Int>): Int {
        return abs(point.first - destination.first) + abs(point.second - destination.second)
    }

}