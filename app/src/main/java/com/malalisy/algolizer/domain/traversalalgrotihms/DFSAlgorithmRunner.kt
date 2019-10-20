package com.malalisy.algolizer.domain.traversalalgrotihms

import java.util.*

class DFSAlgorithmRunner(adjacencyMatrix: Array<Array<Int>>) :
    TraversalAlgorithmRunner(adjacencyMatrix) {

    override fun run() {
        val stack: Stack<Int> = Stack()
        val visited = Array(adjacencyMatrix.size) { false }
        orderedVisitedEdges = mutableListOf()

        stack.push(0)
        visited[0] = true

        while (stack.isNotEmpty()) {
            val node = stack.pop() ?: break
            for (i in 0 until adjacencyMatrix.size) {
                if (adjacencyMatrix[node][i] == 0 || visited[i]) continue
                visited[i] = true
                orderedVisitedEdges.add(node to i)
                stack.push(i)
            }
        }
    }

}