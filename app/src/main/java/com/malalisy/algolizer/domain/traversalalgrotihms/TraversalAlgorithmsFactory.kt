package com.malalisy.algolizer.domain.traversalalgrotihms

import androidx.annotation.IntDef
import java.lang.IllegalArgumentException

class TraversalAlgorithmsFactory {
    companion object {

        const val BFS = 0
        const val DFS = 1

        @IntDef(BFS, DFS)
        @Retention(AnnotationRetention.SOURCE)
        annotation class TraversalAlgorithms

        fun getTraversalAlgorithm(
            @TraversalAlgorithms algorithm: Int,
            adjacencyMatrix: Array<Array<Int>>
        ): TraversalAlgorithmRunner {
            return when (algorithm) {
                BFS -> BFSAlgorithmRunner(adjacencyMatrix)
                DFS -> DFSAlgorithmRunner(adjacencyMatrix)
                else -> throw IllegalArgumentException()
            }
        }
    }
}