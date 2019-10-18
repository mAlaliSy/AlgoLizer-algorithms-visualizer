package com.malalisy.algolizer.domain.mst

import com.malalisy.algolizer.domain.Edge

/**
 * a class that implements the Kruskal's MST algorithm using disjoint-set (aka union-find) data structure
 */
class KruskalAlgorithmRunner(adjacencyMatrix: Array<Array<Int>>) :
    MSTAlgorithmRunner(adjacencyMatrix) {

    private lateinit var p: Array<Int>

    init {
        setup(adjacencyMatrix)
    }


    /**
     * Setup a new problem
     */
    override fun setup(adjacencyMatrix: Array<Array<Int>>) {
        super.setup(adjacencyMatrix)
        p = Array(adjacencyMatrix.size) { it }
    }

    /**
     * Run the algorithm on the current graph
     */
    override fun run() {

        val edges = mutableListOf<Edge>()
        for (i in adjacencyMatrix.indices) {
            for (j in i + 1 until adjacencyMatrix.size) {
                if (adjacencyMatrix[i][j] != 0)
                    edges.add(Edge(i, j, adjacencyMatrix[i][j]))
            }
        }

        edges.sort()

        for (e in edges) {
            val pa = find(e.from)
            val pb = find(e.to)
            if (pa != pb) {
                p[pa] = pb
                mst.add(e.from to e.to)
                mstCost += adjacencyMatrix[e.from][e.to]
            }
        }
    }

    private fun find(u: Int): Int {
        if (u == p[u]) return u
        p[u] = find(p[u])
        return p[u]
    }


}