package com.malalisy.algolizer.domain.traversalalgrotihms

abstract class TraversalAlgorithmRunner(adjacencyMatrix: Array<Array<Int>>) {
    init {
        setup(adjacencyMatrix)
    }

    // A list that contains the edges traversed in order
    lateinit var orderedVisitedEdges: MutableList<Pair<Int, Int>>


    // The graph represented in an adjacency list
    protected lateinit var adjacencyMatrix: Array<Array<Int>>

    public open fun setup(adjacencyMatrix: Array<Array<Int>>) {
        this.adjacencyMatrix = adjacencyMatrix
    }

    abstract fun run()
}