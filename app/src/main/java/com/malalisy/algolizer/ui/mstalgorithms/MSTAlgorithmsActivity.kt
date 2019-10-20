package com.malalisy.algolizer.ui.mstalgorithms

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.github.florent37.viewanimator.ViewAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.malalisy.algolizer.R
import kotlinx.android.synthetic.main.activity_mstalgorithms.*
import kotlinx.android.synthetic.main.activity_mstalgorithms.algorithmSpinner
import kotlinx.android.synthetic.main.activity_mstalgorithms.btnCloseResult
import kotlinx.android.synthetic.main.activity_mstalgorithms.btnPause
import kotlinx.android.synthetic.main.activity_mstalgorithms.btnPlay
import kotlinx.android.synthetic.main.activity_mstalgorithms.btnReset
import kotlinx.android.synthetic.main.activity_mstalgorithms.controls
import kotlinx.android.synthetic.main.activity_mstalgorithms.solutionCostLabel
import kotlinx.android.synthetic.main.activity_mstalgorithms.solutionInfoContainer
import kotlinx.android.synthetic.main.fragment_shortest_path_algorim.*

class MSTAlgorithmsActivity : AppCompatActivity(), MSTAlgorithmsContract.View {
    lateinit var presenter: MSTAlgorithmsContract.Presenter
    var visitedEdgeColor: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mstalgorithms)

        presenter = MSTAlgorithmsPresenter()
        presenter.setupView(this)

        visitedEdgeColor = ContextCompat.getColor(this, R.color.visitedEdgeColor)

        /**
         * Hook up event listeners with the presenter
         */
        btnPlay.setOnClickListener {
            presenter.onRunClicked(graphView.getAdjacencyList())
        }
        btnPause.setOnClickListener { presenter.onPauseClicked() }
        btnReset.setOnClickListener { presenter.onRestartClick() }
        btnCloseResult.setOnClickListener { presenter.onCloseResultClick() }

        val adapter = ArrayAdapter.createFromResource(
            this, R.array.mst_algorithms,
            android.R.layout.simple_spinner_dropdown_item
        )
        algorithmSpinner.adapter = adapter
        algorithmSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                p3: Long
            ) {
                presenter.onAlgorithmChange(position)
            }
        }

    }


    override fun setAcceptGraphChanges(accept: Boolean) {
        graphView.setAcceptInput(accept)
    }

    override fun animateEdge(from: Int, to: Int, duration: Long) {
        graphView.animateEdge(
            from,
            to,
            visitedEdgeColor,
            duration
        )
    }

    override fun resetMSTEdges() {
        graphView.resetAdditionalEdges()
    }

    override fun resetEdges() {
        graphView.resetEdges()
    }

    override fun showHideSolution(show: Boolean, found: Boolean, cost: Int) {
        solutionInfoContainer.visibility = View.VISIBLE
        if (found) {
            solutionInfoContainer.setBackgroundResource(R.drawable.green_rounded_rect)
            solutionCostLabel.text = getString(R.string.mst_found, cost)
        } else {
            solutionInfoContainer.setBackgroundResource(R.drawable.red_rounded_rect)
            solutionCostLabel.setText(R.string.no_mst)
        }
        solutionInfoContainer.scaleX = 0.5f
        solutionInfoContainer.scaleY = 0.5f
        ViewAnimator.animate(solutionInfoContainer)
            .interpolator(AnticipateOvershootInterpolator(1.0f))
            .slideBottomIn()
            .scale(1.0f)
            .fadeIn()
            .duration(500)
            .start()
    }

    override fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

    override fun showHideResetButton(show: Boolean) {
        if (show)
            btnReset.visibility = View.VISIBLE
        ViewAnimator.animate(btnReset)
            .also {
                if (show)
                    it.fadeIn()
                else
                    it.fadeOut()
            }.duration(500)
            .onStop {
                if (!show) btnReset.visibility = View.INVISIBLE
            }
            .start()
    }

    override fun showHidePlayButton(show: Boolean) {
        showHideView(btnPlay, show)
    }


    override fun showHidePauseButton(show: Boolean) {
        showHideView(btnPause, show)
    }

    private fun showHideView(view: View, show: Boolean, duration: Long = 500L) {
        val animate = ViewAnimator.animate(view)
        if (show) {
            view.visibility = View.VISIBLE
            animate.fadeIn()
        } else {
            animate.fadeOut().onStop { view.visibility = View.INVISIBLE }
        }
        animate.duration(duration).start()
    }


    override fun showHideControls(show: Boolean) {
        if (show) {
            ViewAnimator.animate(controls)
                .slideBottomIn()
                .fadeIn()
                .duration(500)
                .interpolator(AnticipateOvershootInterpolator(1.5f))
                .start()
            controls.visibility = View.VISIBLE
        } else {
            ViewAnimator.animate(controls)
                .translationY(controls.height.toFloat())
                .fadeOut()
                .duration(500)
                .onStop {
                    controls.translationY = 0.0f
                    controls.visibility = View.INVISIBLE
                }.start()
        }
    }


    override fun resetGraph() {
        graphView.resetGraph()
    }

}
