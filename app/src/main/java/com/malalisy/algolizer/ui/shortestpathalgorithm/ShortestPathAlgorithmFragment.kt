package com.malalisy.algolizer.ui.shortestpathalgorithm


import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.malalisy.algolizer.R
import kotlinx.android.synthetic.main.fragment_shortest_path_algorim.*

class ShortestPathAlgorithmFragment : Fragment(), ShortestPathAlgorithmContract.View {
    val TAG = "ShortestPathAlgorithm"
    lateinit var presenter: ShortestPathAlgorithmContract.Presenter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shortest_path_algorim, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TransitionManager.beginDelayedTransition(shortestPathAlgorithmContainer)

        presenter = ShortestPathAlgorithmPresenter()
        presenter.setupView(this)

        algoGridView.onGridCellSelected = { i, j ->
            presenter.onItemSelected(i, j)
        }

        btnPlay.setOnClickListener { presenter.onPlayClicked() }
        btnForward.setOnClickListener{presenter.onForwardClicked()}
    }

    override fun animateVisitedItems(vararg cells: Pair<Int, Int>) {
        algoGridView.animateVisitedCells(*cells)
    }

    override fun animateBlockItem(i: Int, j: Int) {
        algoGridView.animateBlockCell(i, j)
    }

    override fun animateSourceItem(i: Int, j: Int) {
        algoGridView.animateSourceCell(i, j)
    }

    override fun animateDestinationItem(i: Int, j: Int) {
        algoGridView.animateDestinationCell(i, j)
    }

    override fun showSolution(solution: List<Pair<Int, Int>>) {
        Log.d(TAG, "Path Found")
    }

    override fun showNoPathFound() {
        Log.d(TAG, "NO Path Found")
    }


    override fun hideDestinationLabel() {
        selectDestinationLabel.visibility = View.GONE
    }

    override fun showDestinationLabel() {
        selectDestinationLabel.visibility = View.VISIBLE
    }

    override fun hideSourceLabel() {
        selectStartLabel.visibility = View.GONE
    }

    override fun showControls() {
        controls.visibility = View.VISIBLE
    }


}
