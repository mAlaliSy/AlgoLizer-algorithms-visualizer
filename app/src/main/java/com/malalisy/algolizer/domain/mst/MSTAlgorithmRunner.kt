package com.malalisy.algolizer.domain.mst

/**
 * an abstract class for Minimum Cost Spanning Tree (MST) algorithms
 */
abstract class MSTAlgorithmRunner {


    // A list that contains the edges of MST
    var mst: List<Pair<Int, Int>>? = null

    // The total cost of MST
    var mstCost: Int = -1

    // The graph represented in an adjacency list
    var adjacencyList:List<List<Pair<Int, Int>>>? = null

}