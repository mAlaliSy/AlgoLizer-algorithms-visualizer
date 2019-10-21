package com.malalisy.algolizer.domain.traversalalgrotihms

import androidx.annotation.IntDef

class TraversalAlgorithmsFactory {
    companion object {

        public const val BFS = 0
        public const val DFS = 1

        @IntDef(BFS, DFS)
        @Retention(AnnotationRetention.SOURCE)
        annotation class TraversalAlgorithms
    }
}