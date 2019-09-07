package com.malalisy.algolizer.ui.shortestpathalgorithm


import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import com.github.florent37.viewanimator.ViewAnimator

import com.malalisy.algolizer.R
import com.malalisy.algolizer.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_shortest_path_algorim.*

class ShortestPathAlgorithmFragment : BaseFragment(), ShortestPathAlgorithmContract.View {
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
        presenter = ShortestPathAlgorithmPresenter()
        presenter.setupView(this)

        algoGridView.onGridCellSelected = { i, j ->
            presenter.onItemSelected(i, j)
        }

        btnPlay.setOnClickListener { presenter.onPlayClicked() }
        btnPause.setOnClickListener { presenter.onPauseClicked() }
        btnForward.setOnClickListener { presenter.onForwardClicked() }
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

    override fun hideSourceLabel() {
        showHideView(selectStartLabel, false)
    }

    override fun showControls() {
        ViewAnimator.animate(controls)
            .slideBottomIn()
            .fadeIn()
            .duration(500)
            .interpolator(AccelerateInterpolator())
            .start()
        controls.visibility = View.VISIBLE
    }

    override fun showHideDestinationLabel(show: Boolean) {
        showHideView(selectDestinationLabel, show)
    }

    override fun showHidePlayButton(show: Boolean) {
        showHideView(btnPlay, show)
    }

    override fun showHidePauseButton(show: Boolean) {
        showHideView(btnPause, show)
    }

    override fun onPause() {
        super.onPause()
        presenter.onViewPause()
    }

}
