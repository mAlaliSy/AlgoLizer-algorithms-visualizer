package com.malalisy.algolizer.domain.traversalalgrotihms

import java.util.*

class BFSAlgorithmRunner(adjacencyMatrix: Array<Array<Int>>) :
    TraversalAlgorithmRunner(adjacencyMatrix) {

    override fun run() {
        val queue: Queue<Int> = LinkedList()
        val visited = Array(adjacencyMatrix.size) { false }
        orderedVisitedEdges = mutableListOf()

        queue.add(0)
        visited[0] = true

        while (queue.isNotEmpty()) {
            val node = queue.poll() ?: break
            for (i in 0 until adjacencyMatrix.size) {
                if (adjacencyMatrix[node][i] == 0 || visited[i]) continue
                visited[i] = true
                orderedVisitedEdges.add(node to i)
                queue.add(i)
            }
        }
    }

}