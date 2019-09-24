package com.malalisy.algolizer.domain.shortestpath

import androidx.annotation.IntDef
import com.malalisy.algolizer.domain.shortestpath.algorithmsimp.AStarAlgorithmRunner
import com.malalisy.algolizer.domain.shortestpath.algorithmsimp.BfsAlgorithmRunner
import com.malalisy.algolizer.domain.shortestpath.algorithmsimp.DijkstraAlgorithmRunner
import com.malalisy.algolizer.domain.shortestpath.algorithmsimp.GreedyBestFirstAlgorithmRunner
import java.lang.annotation.RetentionPolicy


/**
 * Factory pattern implementation for creating shortest path algorithm runner
 */
object ShortestPathAlgorithmsFactory {

    const val DIJKSTRA_ALGORITHM = 0
    const val AStar_ALGORITHM = 1
    const val BFS_ALGORITHM = 2
    const val GREEDY_BEST_FIRST_ALGORITHM = 3

    @IntDef(DIJKSTRA_ALGORITHM, AStar_ALGORITHM, BFS_ALGORITHM, GREEDY_BEST_FIRST_ALGORITHM)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ShortestPathAlgorithm


    fun getAlgorithmRunner(@ShortestPathAlgorithm algorithm: Int, grid: Array<Array<TileType>>)
            : ShortestPathAlgorithmRunner {
        return when (algorithm) {
            DIJKSTRA_ALGORITHM -> DijkstraAlgorithmRunner(grid)
            AStar_ALGORITHM -> AStarAlgorithmRunner(grid)
            BFS_ALGORITHM -> BfsAlgorithmRunner(grid)
            GREEDY_BEST_FIRST_ALGORITHM -> GreedyBestFirstAlgorithmRunner(grid)

            else -> throw IllegalArgumentException("Unsupported algorithm")
        }
    }
}