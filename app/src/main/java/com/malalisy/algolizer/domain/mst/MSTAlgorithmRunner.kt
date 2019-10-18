package com.malalisy.algolizer.domain.mst

/**
 * an abstract class for Minimum Cost Spanning Tree (MST) algorithms
 */
abstract class MSTAlgorithmRunner(adjacencyList: List<List<Pair<Int, Int>>>) {

    init {
        setup(adjacencyList)
    }

    // A list that contains the edges of MST
    lateinit var mst: List<Pair<Int, Int>>

    // The total cost of MST
    var mstCost: Int = -1

    // The graph represented in an adjacency list
    private lateinit var adjacencyList: List<List<Pair<Int, Int>>>


    public open fun setup(adjacencyList: List<List<Pair<Int, Int>>>) {
        this.adjacencyList = adjacencyList
        mst = mutableListOf()
        mstCost = -1
    }

}