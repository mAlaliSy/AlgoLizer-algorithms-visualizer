package com.malalisy.algolizer.domain.mst

import androidx.annotation.IntDef
import java.lang.IllegalArgumentException

object MSTAlgorithmsFactory {
    public const val KRUSKAL_ALGORITHM = 0
    public const val PRIMS_ALGORITHM = 1

    @IntDef(KRUSKAL_ALGORITHM, PRIMS_ALGORITHM)
    @Retention(AnnotationRetention.SOURCE)
    annotation class MSTAlgorithms

    public fun getAlgorithm(
        @MSTAlgorithms algorithm: Int,
        adjacencyMatrix: Array<Array<Int>>
    ): MSTAlgorithmRunner {
        return when (algorithm) {
            KRUSKAL_ALGORITHM -> KruskalAlgorithmRunner(adjacencyMatrix)
            PRIMS_ALGORITHM -> PrimAlgorithmRunner(adjacencyMatrix)
            else -> throw IllegalArgumentException()
        }
    }
}