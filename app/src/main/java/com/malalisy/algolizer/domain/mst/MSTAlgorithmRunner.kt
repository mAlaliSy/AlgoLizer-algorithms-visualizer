package com.malalisy.algolizer.domain.mst

/**
 * an abstract class for Minimum Cost Spanning Tree (MST) algorithms
 */
abstract class MSTAlgorithmRunner(adjacencyMatrix: Array<Array<Int>>) {

    init {
        setup(adjacencyMatrix)
    }

    // A list that contains the edges of MST
    lateinit var mst: MutableList<Pair<Int, Int>>

    // The total cost of MST
    var mstCost: Int = -1

    // The graph represented in an adjacency list
    protected lateinit var adjacencyMatrix: Array<Array<Int>>

    public open fun setup(adjacencyMatrix: Array<Array<Int>>) {
        this.adjacencyMatrix = adjacencyMatrix
        mst = mutableListOf()
        mstCost = 0
    }

    abstract fun run()

}