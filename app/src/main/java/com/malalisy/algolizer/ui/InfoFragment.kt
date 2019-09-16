package com.malalisy.algolizer.ui


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.malalisy.algolizer.GITHUB_REPO

import com.malalisy.algolizer.R
import com.malalisy.algolizer.TWITTER_ACCOUNT
import kotlinx.android.synthetic.main.fragment_info.*

class InfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnGithubRepo.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = GITHUB_REPO.toUri()
            startActivity(Intent.createChooser(intent, "Open with"))
        }
        btnTwitter.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = TWITTER_ACCOUNT.toUri()
            startActivity(Intent.createChooser(intent, "Open with"))
        }
    }


}
