package com.stato.jewintage.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.model.AddSales
import com.stato.jewintage.model.DbManager

class FirebaseViewModel : ViewModel() {
    private val dbManager = DbManager()
    val liveAdsData = MutableLiveData<ArrayList<AddNom>>()
    val liveSalesData = MutableLiveData<ArrayList<AddSales>>()

//    fun updateNom(updatedNom: AddNom) {
//        val ref = dbManager.dbSales.child(updatedNom.id!!)
//
//        ref.setValue(updatedNom)
//            .addOnSuccessListener {
//                Log.d("UpdateNom", "DocumentSnapshot successfully updated!")
//            }
//            .addOnFailureListener { e ->
//                Log.w("UpdateNom", "Error updating document", e)
//            }
//    }


    fun loadAllSales() {
        dbManager.getAllSales(object : DbManager.ReadSalesDataCallback {
            override fun readData(list: ArrayList<AddSales>) {
                liveSalesData.value = list
            }
        })
    }

    fun loadAllAds(){
        dbManager.getAllAds(object : DbManager.ReadDataCallBack{
            override fun readData(list: ArrayList<AddNom>) {
                liveAdsData.value = list
            }

        })
    }

    fun deleteItem(addNom: AddNom){
        dbManager.deleteAd(addNom, object : DbManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                val updatedList = liveAdsData.value
                updatedList?.remove(addNom)
                liveAdsData.postValue(updatedList!!)
            }
        })
    }
    fun deleteSellItem(addSales: AddSales){
        dbManager.deleteSellAd(addSales, object : DbManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                val updatedList = liveSalesData.value
                updatedList?.remove(addSales)
                liveSalesData.postValue(updatedList!!)
            }
        })
    }
}