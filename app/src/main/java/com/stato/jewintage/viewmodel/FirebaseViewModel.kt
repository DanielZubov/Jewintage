package com.stato.jewintage.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.stato.jewintage.model.AddCost
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.model.AddSales
import com.stato.jewintage.model.Category
import com.stato.jewintage.model.DbManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class FirebaseViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val dbManager = DbManager()
    val liveCategoryData = MutableLiveData<ArrayList<Category>>()
    val liveAdsData = MutableLiveData<ArrayList<AddNom>>()
    val liveSalesData = MutableLiveData<ArrayList<AddSales>>()
    val liveCostData = MutableLiveData<ArrayList<AddCost>>()
    private var allCategoryData = ArrayList<Category>()
    private var allAdsData = ArrayList<AddNom>()
    private var allSalesData = ArrayList<AddSales>()
    private var allCostData = ArrayList<AddCost>()
    private val _salesGroupList = MutableLiveData<List<String>>()
    var onCategoryDeletedListener: OnCategoryDeletedListener? = null
    val salesGroupList: LiveData<List<String>> get() = _salesGroupList


    fun updateCommissionCard(commission: Float) {
        val uid = auth.currentUser?.uid ?: return
        dbManager.updateCommissionCard(uid, commission)
    }

    fun updateCommissionCash(commission: Float) {
        val uid = auth.currentUser?.uid ?: return
        dbManager.updateCommissionCash(uid, commission)
    }

    fun loadAllCategories() {
        if (auth.currentUser != null) {
            dbManager.getAllCategories(object : DbManager.ReadCategoryDataCallback {
                override fun readData(list: ArrayList<Category>) {
                    allCategoryData = list
                    liveCategoryData.value = list
                }
            })
        } else {
            liveCategoryData.value = ArrayList()
        }
    }
    fun updateCategory(category: Category) {
        if (auth.currentUser != null) {
            dbManager.updateCategory(category, object : DbManager.FinishWorkListener {
                override fun onFinish(isDone: Boolean) {
                    if (isDone) {
                        val updatedList = liveCategoryData.value
                        val index = updatedList?.indexOfFirst { it.id == category.id }
                        if (index != null && index >= 0) {
                            updatedList[index] = category
                            liveCategoryData.postValue(updatedList!!)
                        }
                    } else {
                        Log.d("MyLog", "Failed to update category")
                    }
                }
            })
        }
    }


    fun deleteCategoryItem(category: Category, position: Int) {
        dbManager.deleteCategory(category, object : DbManager.FinishWorkListener {
            override fun onFinish(isDone: Boolean) {
                if (isDone) {
                    val updatedList = liveCategoryData.value
                    updatedList?.remove(category)
                    liveCategoryData.postValue(updatedList!!)
                    // Notify about removed item
                    onCategoryDeletedListener?.onCategoryDeleted(position)
                }
            }
        })
    }

    fun filterAds(
        checkedCategories: Set<Category>,
        minPrice: Double?,
        maxPrice: Double?,
        dateFrom: String?,
        dateTo: String?
    ) {
        val filteredList = allAdsData.filter { ad ->
            val isCategoryMatch = checkedCategories.isEmpty() || checkedCategories.any { it.name == ad.category }
            val isPriceInRange = (minPrice == null || (ad.price != null && ad.price.toDouble() >= minPrice)) && (maxPrice == null || (ad.price != null && ad.price.toDouble() <= maxPrice))
            val isDateInRange = isDateInRange(ad.date!!, dateFrom ?: "", dateTo ?: "")

            isCategoryMatch && isPriceInRange && isDateInRange
        }

        liveAdsData.value = ArrayList(filteredList)
    }
    fun filterSalesAds(
        checkedCategories: Set<Category>,
        minPrice: Double?,
        maxPrice: Double?,
        dateFrom: String?,
        dateTo: String?
    ) {
        val filteredList = allSalesData.filter { sale ->
            val isCategoryMatch = checkedCategories.isEmpty() || checkedCategories.any { it.name == sale.category }
            val isPriceInRange = (minPrice == null || (sale.price != null && sale.price.toDouble() >= minPrice)) && (maxPrice == null || (sale.price != null && sale.price.toDouble() <= maxPrice))
            val isDateInRange = isDateInRange(sale.date!!, dateFrom ?: "", dateTo ?: "")

            isCategoryMatch && isPriceInRange && isDateInRange
        }

        liveSalesData.value = ArrayList(filteredList)
    }
    fun filterCostAds(
        checkedCategories: Set<Category>,
        minPrice: Double?,
        maxPrice: Double?,
        dateFrom: String?,
        dateTo: String?
    ) {
        val filteredList = allCostData.filter { cost ->
            val isCategoryMatch = checkedCategories.isEmpty() || checkedCategories.any { it.name == cost.category }
            val isPriceInRange = (minPrice == null || (cost.price != null && cost.price.toDouble() >= minPrice)) && (maxPrice == null || (cost.price != null && cost.price.toDouble() <= maxPrice))
            val isDateInRange = isDateInRange(cost.date!!, dateFrom ?: "", dateTo ?: "")

            isCategoryMatch && isPriceInRange && isDateInRange
        }

        liveCostData.value = ArrayList(filteredList)
    }


    fun loadAllAds() {
        if (auth.currentUser != null) {
            dbManager.getAllAds(object : DbManager.ReadNomDataCallback {
                override fun readData(list: ArrayList<AddNom>) {
                    allAdsData = list
                    liveAdsData.value = list
                }
            })
        } else {
            liveAdsData.value = ArrayList()
        }
    }

    fun loadAllSales(date: String?) {
        if (auth.currentUser != null) {
            dbManager.getAllSales(object : DbManager.ReadSalesDataCallback {
                override fun readData(list: ArrayList<AddSales>) {
                    allSalesData = list
                    filterSalesByDate(date) // переместили эту строку сюда
                }
            })
        } else {
            liveSalesData.value = ArrayList()
        }
    }

    fun loadSalesGroupDates() {
        if (auth.currentUser != null) {
            dbManager.getAllSales(object : DbManager.ReadSalesDataCallback {
                override fun readData(list: ArrayList<AddSales>) {
                    val dates = list.mapNotNull { it.date }.toMutableList()
                    dates.add(0, "Все")
                    val sortedDistinctDates = dates.distinct().sorted()
                    _salesGroupList.value = sortedDistinctDates
                }
            })
        } else {
            _salesGroupList.value = emptyList()
        }
    }




    fun filterSalesByDate(date: String?) {
        val filteredList = if (date == "Все" || date == null) {
            allSalesData
        } else {
            allSalesData.filter { sale -> sale.date == date }
        }

        liveSalesData.value = ArrayList(filteredList)
    }

    fun loadAllCost() {
        if (auth.currentUser != null) {
            dbManager.getAllCosts(object : DbManager.ReadCostDataCallback {
                override fun readData(list: ArrayList<AddCost>) {
                    allCostData = list
                    liveCostData.value = list
                }
            })
        } else {
            liveCostData.value = ArrayList()
        }
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
    fun deleteCostItem(cost: AddCost){
        dbManager.deleteCostAd(cost, object : DbManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                val updatedList = liveCostData.value
                updatedList?.remove(cost)
                liveCostData.postValue(updatedList!!)
            }
        })
    }
    fun deleteSellItem(addSales: AddSales){
        dbManager.deleteSellAd(addSales, object : DbManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                val updatedList = liveSalesData.value ?: arrayListOf()
                updatedList.remove(addSales)
                liveSalesData.postValue(updatedList)
            }
        })
    }
    private fun isDateInRange(adDate: String, dateFrom: String?, dateTo: String?): Boolean {
        val adDateMillis = parseDate(adDate)
        val dateFromMillis = dateFrom?.let { parseDate(it) }
        val dateToMillis = dateTo?.let { parseDate(it) }

        return if (adDateMillis != null) {
            (dateFromMillis == null || adDateMillis >= dateFromMillis) && (dateToMillis == null || adDateMillis <= dateToMillis)
        } else {
            false
        }
    }
    private fun parseDate(date: String): Long? {
        return try {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            dateFormat.parse(date)?.time
        } catch (e: ParseException) {
            null
        }
    }
    interface OnCategoryDeletedListener {
        fun onCategoryDeleted(position: Int)
    }
}