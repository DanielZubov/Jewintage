package com.stato.jewintage.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

open class BaseFrag: Fragment(), onClose {




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAds()
    }
    private fun initAds(){
    }

    override fun onClose() {}
}