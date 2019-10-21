package com.malalisy.algolizer.domain.traversalalgrotihms

import java.util.*

class DFSAlgorithmRunner(adjacencyMatrix: Array<Array<Int>>) :
    TraversalAlgorithmRunner(adjacencyMatrix) {

    lateinit var visited: Array<Boolean>

    override fun run() {

        visited = Array(adjacencyMatrix.size) { false }
        orderedVisitedEdges = mutableListOf()

        for (i in adjacencyMatrix.indices) {
            if (visited[i]) continue
            visited[i] = true
            dfs(i)
        }
    }

    private fun dfs(index: Int) {

        for (i in adjacencyMatrix.indices) {
            if (adjacencyMatrix[index][i] == 0 || visited[i]) continue
            visited[i] = true
            orderedVisitedEdges.add(index to i)
            dfs(i)
        }
    }

}