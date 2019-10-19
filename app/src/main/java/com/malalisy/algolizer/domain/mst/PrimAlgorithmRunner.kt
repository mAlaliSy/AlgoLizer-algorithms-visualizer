package com.malalisy.algolizer.domain.mst

import android.util.Log
import java.util.*

class PrimAlgorithmRunner(adjacencyMatrix: Array<Array<Int>>) :
    MSTAlgorithmRunner(adjacencyMatrix) {

    lateinit var visited: Array<Boolean>

    override fun setup(adjacencyMatrix: Array<Array<Int>>) {
        super.setup(adjacencyMatrix)
        visited = Array(adjacencyMatrix.size) { false }
    }

    override fun run() {
        val queue = PriorityQueue<Vertex>()

        queue.add(Vertex(0, 0, -1))

        while (queue.isNotEmpty()) {
            val node = queue.poll()
            if (visited[node.index]) continue
            // Add the edge from parent to the node to the MST
            if (node.parent != -1) {
                mst.add(node.parent to node.index)
                mstCost += adjacencyMatrix[node.parent][node.index]
            }
            visited[node.index] = true
            for (i in adjacencyMatrix.indices) {
                if (adjacencyMatrix[node.index][i] != 0) {
                    queue.add(Vertex(i, adjacencyMatrix[node.index][i], node.index))
                }
            }
        }

    }


    data class Vertex(val index: Int, var cost: Int, var parent: Int) : Comparable<Vertex> {
        override fun compareTo(other: Vertex) = cost.compareTo(other.cost)
    }
}