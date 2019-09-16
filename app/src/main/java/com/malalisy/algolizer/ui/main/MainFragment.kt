package com.malalisy.algolizer.ui.main


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.malalisy.algolizer.R
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * A simple [Fragment] subclass.
 */
class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnShortestPathAlgorithms.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_mainFragment_to_shortestPathAlgorimFragment
            )
        )

        btnInfo.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_mainFragment_to_infoFragment)
        )
    }


}
