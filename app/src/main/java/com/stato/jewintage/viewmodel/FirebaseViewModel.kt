package com.stato.jewintage.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.model.DbManager

class FirebaseViewModel : ViewModel() {
    private val dbManager = DbManager()
    val liveAdsData = MutableLiveData<ArrayList<AddNom>>()

    public fun loadAllAds(){
        dbManager.readDataFromDb(object : DbManager.ReadDataCallBack{
            override fun readData(list: ArrayList<AddNom>) {
                liveAdsData.value = list
            }

        })
    }
}