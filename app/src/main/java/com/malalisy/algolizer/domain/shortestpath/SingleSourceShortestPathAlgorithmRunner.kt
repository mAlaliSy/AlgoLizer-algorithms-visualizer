package com.malalisy.algolizer.domain.shortestpath


/**
 * A single source algorithm runner base class
 *
 * @constructor
 *
 *
 * @param grid
 * @param source
 */
abstract class SingleSourceShortestPathAlgorithmRunner(
    grid: Array<Array<TileType>>,
    source: Pair<Int, Int>
) :
    ShortestPathAlgorithmRunner() {

    init {
        super.setup(grid)
    }

    abstract fun setup(grid: Array<Array<TileType>>, source: Pair<Int, Int>)
}