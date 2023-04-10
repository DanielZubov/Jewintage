package com.stato.jewintage.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

open class BaseFrag: Fragment(), onClose {




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAds()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initAds(){
    }

    override fun onClose() {}
}