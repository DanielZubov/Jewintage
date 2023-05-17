package com.stato.jewintage.model

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DbManager {

    val db = Firebase.database.getReference(NOM_NODE)
    val dbSales = Firebase.database.getReference(SALE_NODE)
    val dbCosts = Firebase.database.getReference(COST_NODE)
    val dbStorage = Firebase.storage.getReference(NOM_NODE)
    val dbCategories = Firebase.database.getReference(CATEGORY_NODE)
    val dbCommissions = Firebase.database.getReference(COMMISSIONS_NODE)
    val auth = Firebase.auth

    // Функция для получения комиссии категории
    suspend fun getCommissionCategory(uid: String, categoryName: String): Float? {
        return withContext(Dispatchers.IO) {
            val snapshot = dbCategories
                .child(uid)
                .orderByChild("name")
                .equalTo(categoryName)
                .get()
                .await()

            if (snapshot.exists()) {
                // Если категория найдена, извлечь значение комиссии
                val commissionValue =
                    snapshot.children.firstOrNull()?.child("commission")?.value.toString()
                        .toFloatOrNull()
                commissionValue ?: 0f // Если значение не установлено, возвращаем 0
            } else {
                null
            }
        }
    }

    // Функция для получения комиссии способа оплаты
    suspend fun getCommissionPaymentMethod(uid: String, paymentMethod: String): Float? {
        val dbRef = dbCommissions.child(uid)
        return withContext(Dispatchers.IO) {
            val snapshot = dbRef.get().await()
            if (snapshot.exists()) {
                when (paymentMethod) {
                    "Cash", "Наличка" -> snapshot.child("commissionCash")
                        .getValue(Float::class.java)

                    "Visa/MasterCard" -> snapshot.child("commissionCard")
                        .getValue(Float::class.java)

                    else -> null
                }
            } else {
                null
            }
        }
    }


    fun updateCommissionCard(uid: String, commission: Float) {
        dbCommissions.child(uid).child("commissionCard").setValue(commission)
    }

    fun updateCommissionCash(uid: String, commission: Float) {
        dbCommissions.child(uid).child("commissionCash").setValue(commission)
    }

    fun getAllCategories(callback: ReadCategoryDataCallback) {
        val query = dbCategories.child(auth.uid!!)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list = ArrayList<Category>()
                for (postSnapshot in dataSnapshot.children) {
                    val category = postSnapshot.getValue(Category::class.java)
                    if (category != null) {
                        list.add(category)
                    }
                }
                callback.readData(list)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MyApp", "loadCategories:onCancelled", databaseError.toException())
            }
        })
    }

    fun updateCategory(category: Category, finishWorkListener: FinishWorkListener) {
        if (auth.uid != null) {
            dbCategories.child(auth.uid!!)
                .child(category.id!!)
                .setValue(category)
                .addOnCompleteListener { task ->
                    finishWorkListener.onFinish(task.isSuccessful)
                }
        }
    }

    fun addNewCategory(category: Category, finishWorkListener: FinishWorkListener) {
        if (auth.uid != null)
            dbCategories.child(auth.uid!!)
                .child(category.id ?: "empty")
                .setValue(category).addOnCompleteListener {
                    finishWorkListener.onFinish(it.isSuccessful)
                }
    }

    fun updateQuantity(addNom: AddNom, newQuantity: String, listener: FinishWorkListener) {
        if (addNom.id == null || addNom.uid == null) return
        db.child(addNom.uid).child(addNom.id).child(AD_NODE).child("quantity")
            .setValue(newQuantity).addOnCompleteListener {
                if (it.isSuccessful) listener.onFinish(true)
            }
    }

    fun calculateTotalSalesByDate(date: String, callback: (Double) -> Unit) {
        val uid = auth.uid ?: return


        val salesReference = dbSales.child(uid)
        salesReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var totalSum = 0.0
                for (snapshot in dataSnapshot.children) {
                    val sale = snapshot.child(AD_NODE).getValue(AddSales::class.java)
                    if (date == "Все" || sale?.date == date) {
                        totalSum += sale?.price?.toDoubleOrNull() ?: 0.0
                    }
                }
                callback(totalSum)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("DbManager", "Failed to read value.", databaseError.toException())
            }
        })
    }


    fun updateSale(
        sale: AddSales,
        updatedSellPrice: String,
        updatedSellDate: String,
        updatedQuantity: String,
        paymentMethod: String,
        listener: FinishWorkListener
    ) {

        val saleRef = dbSales.child(sale.uid!!).child(sale.id!!).child(AD_NODE)

        val updates = hashMapOf<String, Any>(
            "price" to updatedSellPrice,
            "date" to updatedSellDate,
            "soldQuantity" to updatedQuantity,
            "paymentMethod" to paymentMethod
        )

        saleRef.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                listener.onFinish(true)
            } else {
                listener.onFinish(false)
            }
        }
    }
    fun updateCost(cost: AddCost, updatedCostPrice: String, updatedCostDate: String, updatedQuantity: String, updatedCostCategory: String,updatedCostDes: String, listener: FinishWorkListener) {

        val saleRef = dbCosts.child(cost.id!!)

        val updates = hashMapOf<String, Any>(
            "price" to updatedCostPrice,
            "date" to updatedCostDate,
            "quantity" to updatedQuantity,
            "category" to updatedCostCategory,
            "description" to updatedCostDes
        )

        saleRef.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                listener.onFinish(true)
            } else {
                listener.onFinish(false)
            }
        }
    }

    fun publishAdd(addNom: AddNom, finishWorkListener: FinishWorkListener) {
        if (auth.uid != null)
            db.child(auth.uid!!)
                .child(addNom.id ?: "empty")
                .child(AD_NODE)
                .setValue(addNom).addOnCompleteListener {
                    finishWorkListener.onFinish(it.isSuccessful)
                }
    }

    fun publishCost(cost: AddCost, finishWorkListener: FinishWorkListener) {
        if (auth.uid != null)
            dbCosts.child(auth.uid!!)
                .child(cost.id ?: "empty")
                .child(AD_NODE)
                .setValue(cost).addOnCompleteListener {
                    finishWorkListener.onFinish(it.isSuccessful)
                }
    }

    fun saveSale(sale: AddSales, onFinish: FinishWorkListener) {
        val saleRef = dbSales.child(sale.uid!!).child(sale.id!!).child(AD_NODE)
        saleRef.setValue(sale.toMap()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onFinish.onFinish(true)
            } else {
                onFinish.onFinish(false)
            }
        }
    }

    fun getAllAds(callback: ReadNomDataCallback) {
        val query = db.child(auth.uid!!)
        readDataFromDb(query, callback, AddNom::class.java)
    }

    fun getAllSales(callback: ReadSalesDataCallback) {
        val query = dbSales.child(auth.uid!!)
        readDataFromDb(query, callback, AddSales::class.java)
    }

    fun getAllCosts(callback: ReadCostDataCallback) {
        val query = dbCosts.child(auth.uid!!)
        readDataFromDb(query, callback, AddCost::class.java)
    }


    fun deleteAd(addNom: AddNom, listener: FinishWorkListener) {
        if (addNom.id == null || addNom.uid == null) return
        db.child(addNom.uid).child(addNom.id).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish(true)

        }
    }


    fun deleteSellAd(sale: AddSales, listener: FinishWorkListener) {
        if (sale.id == null || sale.uid == null) return
        dbSales.child(sale.uid).child(sale.id).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish(true)
        }
    }

    fun deleteCategory(category: Category, listener: FinishWorkListener) {
        if (category.id == null || auth.uid == null) return
        dbCategories.child(auth.uid!!).child(category.id!!).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish(true)
        }
    }


    fun deleteCostAd(cost: AddCost, onFinish: FinishWorkListener) {
        if (cost.id == null || cost.uid == null) return
        dbCosts.child(cost.uid).child(cost.id).removeValue().addOnCompleteListener {
            if (it.isSuccessful) onFinish.onFinish(true)
        }
    }

    private fun <T> readDataFromDb(
        query: Query,
        callback: ReadDataCallback<T>,
        dataClass: Class<T>
    ) {
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataList = ArrayList<T>()
                for (item in snapshot.children) {
                    val data = item.child("Item").getValue(dataClass)
                    if (data != null) dataList.add(data)
                }
                callback.readData(dataList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    fun findSaleByDate(
        uid: String,
        itemId: String,
        saleDate: String,
        paymentMethod: String,
        listener: FindSaleListener
    ) {
        val saleReference = dbSales.child(uid)
        saleReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val sale = snapshot.child("Item").getValue(AddSales::class.java)
                    if (sale?.idItem == itemId && sale.date == saleDate && sale.paymentMethod == paymentMethod) {
                        listener.onFinish(snapshot.key, sale)
                        return
                    }
                }

                listener.onFinish(null, null)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFinish(null, null)
                Log.e(
                    "readDataFromDb",
                    "Error findSaleByDate from database",
                    databaseError.toException()
                )
            }
        })
    }

    fun getImagesFromDatabase(uid: String, saleId: String, callback: (List<String>) -> Unit) {
        val database = dbSales.child(uid).child(saleId).child("Item")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val imageUrls = mutableListOf<String>()
                dataSnapshot.child("mainImage").value?.let { mainImageUrl ->
                    imageUrls.add(mainImageUrl.toString())
                }
                dataSnapshot.child("image2").value?.let { image2Url ->
                    imageUrls.add(image2Url.toString())
                }
                dataSnapshot.child("image3").value?.let { image3Url ->
                    imageUrls.add(image3Url.toString())
                }

                callback(imageUrls)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(
                    "MyDebugTag",
                    "Error fetching images from database",
                    databaseError.toException()
                )
            }
        })
    }


    fun updateSaleQuantityAndPrice(
        uid: String,
        saleKey: String,
        newQuantity: String,
        newPrice: String,
        finishWorkListener: FinishWorkListener
    ) {
        val saleReference = dbSales.child(uid).child(saleKey).child("Item")

        saleReference.updateChildren(
            mapOf(
                "soldQuantity" to newQuantity,
                "price" to newPrice
            )
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                finishWorkListener.onFinish(true)
            } else {
                finishWorkListener.onFinish(false)
            }
        }
    }

    interface FindSaleListener {
        fun onFinish(saleKey: String?, sale: AddSales?)
    }

    interface ReadDataCallback<T> {
        fun readData(list: ArrayList<T>)
    }

    interface ReadNomDataCallback : ReadDataCallback<AddNom>
    interface ReadSalesDataCallback : ReadDataCallback<AddSales>
    interface ReadCostDataCallback : ReadDataCallback<AddCost>
    interface ReadCategoryDataCallback : ReadDataCallback<Category>

    interface FinishWorkListener {
        fun onFinish(isDone: Boolean)
    }


    companion object {
        const val AD_NODE = "Item"
        const val CATEGORY_NODE = "Categories"
        const val COMMISSIONS_NODE = "Commissions"
        const val NOM_NODE = "Nomenclature"
        const val SALE_NODE = "Sales"
        const val COST_NODE = "Costs"


    }

}