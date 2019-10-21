package com.malalisy.algolizer.ui


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.malalisy.algolizer.R
import com.malalisy.algolizer.ui.mstalgorithms.MSTAlgorithmsActivity
import com.malalisy.algolizer.ui.traversalalgorithms.TraversalAlgorithmsActivity
import kotlinx.android.synthetic.main.fragment_other_graph_algorithms.*

/**
 * A simple [Fragment] subclass.
 */
class OtherGraphAlgorithmsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_other_graph_algorithms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnMST.setOnClickListener {
            startActivity(Intent(context!!, MSTAlgorithmsActivity::class.java))
        }
        btnGraphTraversal.setOnClickListener {
            startActivity(Intent(context!!, TraversalAlgorithmsActivity::class.java))
        }
    }


}
