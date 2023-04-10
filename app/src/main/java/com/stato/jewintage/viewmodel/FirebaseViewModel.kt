package com.stato.jewintage.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.model.DbManager

class FirebaseViewModel : ViewModel() {
    private val dbManager = DbManager()
    val liveAdsData = MutableLiveData<ArrayList<AddNom>>()

    fun loadAllAds(){
        dbManager.getAllAds(object : DbManager.ReadDataCallBack{
            override fun readData(list: ArrayList<AddNom>) {
                liveAdsData.value = list
            }

        })
    }
    fun loadMyAds(){
        dbManager.getAllAds(object : DbManager.ReadDataCallBack{
            override fun readData(list: ArrayList<AddNom>) {
                liveAdsData.value = list
            }

        })
    }
    fun deleteItem(addNom: AddNom){
        dbManager.deleteAd(addNom, object : DbManager.FinishWorkListener{
            override fun onFinish() {
                val updatedList = liveAdsData.value
                updatedList?.remove(addNom)
                liveAdsData.postValue(updatedList!!)
            }

        })
    }
}