package com.malalisy.algolizer.domain

data class Edge(val from: Int, val to: Int, val weight: Int) : Comparable<Edge> {
    override fun compareTo(other: Edge) = weight.compareTo(other.weight)
}