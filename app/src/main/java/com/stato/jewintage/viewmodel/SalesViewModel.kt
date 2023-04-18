package com.stato.jewintage.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stato.jewintage.model.AddSales
import com.stato.jewintage.model.DbManager

class SalesViewModel : ViewModel() {
    private val dbManager = DbManager()
    val liveSalesData = MutableLiveData<ArrayList<AddSales>>()

    fun loadAllSales() {
        dbManager.getAllSales(object : DbManager.ReadSalesDataCallback {
            override fun readData(list: ArrayList<AddSales>) {
                liveSalesData.value = list
            }
        })
    }
}