package com.malalisy.algolizer.ui.shortestpathalgorithm


import android.os.Bundle
import android.os.Handler
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.SeekBar
import com.github.florent37.viewanimator.ViewAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior

import com.malalisy.algolizer.R
import com.malalisy.algolizer.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_shortest_path_algorim.*

class ShortestPathAlgorithmFragment : BaseFragment(), ShortestPathAlgorithmContract.View {
    val TAG = "ShortestPathAlgorithm"

    lateinit var presenter: ShortestPathAlgorithmContract.Presenter

    var bottomSheetDraggingOrExpanded = false

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

        algoGridView.onGridCellStartTouch = { i, j ->
            if (!bottomSheetDraggingOrExpanded)
                presenter.onCellStartTouch(i, j)
        }

        algoGridView.onGridCellTouchMove = { i, j ->
            if (!bottomSheetDraggingOrExpanded)
                presenter.onCellTouchMove(i, j)
        }

        val bottomSheetBehavior = BottomSheetBehavior.from(controls)
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
                bottomSheetDraggingOrExpanded = true
            }

            override fun onStateChanged(view: View, state: Int) {
                bottomSheetDraggingOrExpanded = state != BottomSheetBehavior.STATE_COLLAPSED
            }

        })


        /**
         * Hook up event listeners with the presenter
         */
        btnPlay.setOnClickListener { presenter.onPlayClicked() }
        btnPause.setOnClickListener { presenter.onPauseClicked() }
        btnForward.setOnClickListener { presenter.onForwardClicked() }
        speedContorller.onSpeedChangeListener = { presenter.onSpeedChanged(it) }
        btnReplay.setOnClickListener { presenter.onRestartClick() }
        animationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2)
                    presenter.onAnimationSeekBarChanged(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
        interactiveSwitch.setOnCheckedChangeListener { _, checked ->
            presenter.onInteractiveCheckChange(checked)
        }
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

    override fun animateSolutionCells(vararg cells: Pair<Int, Int>) {
        algoGridView.animateSolutionCells(*cells)
    }

    override fun animateDestinationItem(i: Int, j: Int) {
        algoGridView.animateDestinationCell(i, j)
    }

    override fun showHideControls(show: Boolean) {
        if (show) {
            ViewAnimator.animate(controls)
                .slideBottomIn()
                .fadeIn()
                .duration(1000)
                .interpolator(AnticipateOvershootInterpolator(2.0f))
                .start()
            controls.visibility = View.VISIBLE
        } else {
            ViewAnimator.animate(controls)
                .translationY(controls.height.toFloat())
                .fadeOut()
                .duration(1000)
                .interpolator(AnticipateOvershootInterpolator(2.0f))
                .onStop {
                    controls.translationY = 0.0f
                    controls.visibility = View.INVISIBLE
                }.start()
        }
    }

    override fun showHideSourceLabel(show: Boolean) {
        showHideView(selectStartLabel, show)
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

    override fun showHideResultContainer(show: Boolean, solutionFound: Boolean, solutionCost: Int) {
        if (show) {
            solutionInfoContainer.visibility = View.VISIBLE
            if (solutionFound) {
                solutionInfoContainer.setBackgroundResource(R.drawable.green_rounded_rect)
                solutionCostLabel.setText(getString(R.string.solution_found, solutionCost))
            } else {
                solutionInfoContainer.setBackgroundResource(R.drawable.red_rounded_rect)
                solutionCostLabel.setText(R.string.no_path_found)
            }
            solutionInfoContainer.scaleX = 0.5f
            solutionInfoContainer.scaleY = 0.5f
            ViewAnimator.animate(solutionInfoContainer)
                .interpolator(AnticipateOvershootInterpolator(1.0f))
                .slideBottomIn()
                .scale(1.0f)
                .fadeIn()
                .duration(1000)
                .start()
        } else {
            ViewAnimator.animate(solutionInfoContainer)
                .translationY(solutionInfoContainer.height.toFloat())
                .scale(0.0f)
                .fadeOut()
                .duration(1000)
                .interpolator(AnticipateOvershootInterpolator(1.0f))
                .onStop { solutionInfoContainer.translationY = 0f }
                .start()
        }
    }

    override fun clearGrid() {
        algoGridView.clearGrid(true)
    }

    override fun setAnimationSeekBarMaxValue(maxValue: Int) {
        animationSeekBar.max = maxValue
    }

    override fun showHideAnimationSeekBar(show: Boolean) {
        val animator = ViewAnimator.animate(animationSeekBar).duration(200)
        if (show) {
            animationSeekBar.visibility = View.VISIBLE
            animator.fadeIn()
        } else animator.fadeOut().onStop { animationSeekBar.visibility = View.INVISIBLE }
        animator.start()
    }

    override fun setAnimationSeekBarValue(value: Int) {
        animationSeekBar.progress = value
    }

    override fun animateRemoveVisitedItems(vararg cells: Pair<Int, Int>) {
        algoGridView.animateRemoveVisitedItems(*cells)
    }

    override fun showHideInteractiveModeButton(show: Boolean) {
        showHideView(interactiveSwitch, show)
    }

    override fun animateRemoveDestinationCell(destination: Pair<Int, Int>) {
        algoGridView.animateRemoveDestinationCell(destination)
    }

    override fun animateRemoveSolutionCells(vararg cells: Pair<Int, Int>) {
        algoGridView.animateRemoveSolutionCells(*cells)
    }

    override fun setInteractiveMode(interactive: Boolean) {
        interactiveSwitch.isChecked = interactive
    }
}
