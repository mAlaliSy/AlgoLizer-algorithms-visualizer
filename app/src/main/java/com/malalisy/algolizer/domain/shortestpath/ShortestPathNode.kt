package com.malalisy.algolizer.domain.shortestpath

/**
 * A class that represent a node in the execution of a shortest path algorithm
 *
 * @property position the position of the node
 * @property distance distance to the root
 * @property parent the predecessor of node (the node that we reached this node from)
 */
data class ShortestPathNode(
    val position: Pair<Int, Int>,
    val distance: Int,
    val parent: ShortestPathNode?
)