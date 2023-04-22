package com.stato.jewintage.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class DbManager {

    val db = Firebase.database.getReference(MAIN_NODE)
    val dbSales = Firebase.database.getReference(SALE_NODE)
    val dbStorage = Firebase.storage.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun updateQuantity(addNom: AddNom, newQuantity: String, listener: FinishWorkListener) {
        if (addNom.id == null || addNom.uid == null) return
        db.child(addNom.id).child(addNom.uid).child(AD_NODE).child("quantity")
            .setValue(newQuantity).addOnCompleteListener {
                if (it.isSuccessful) listener.onFinish(true)
            }
    }

    fun publishAdd(addNom: AddNom, finishWorkListener: FinishWorkListener) {
        if (auth.uid != null)
            db.child(addNom.id ?: "empty")
                .child(auth.uid!!)
                .child(AD_NODE)
                .setValue(addNom).addOnCompleteListener {
                    finishWorkListener.onFinish(it.isSuccessful)
                }
    }

    fun saveSale(sale: AddSales, onFinish: FinishWorkListener) {
        val saleRef = dbSales.child(sale.idItem!!)
        saleRef.setValue(sale.toMap()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onFinish.onFinish(true)
            } else {
                onFinish.onFinish(false)
            }
        }
    }

    fun getAllAds(readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild(auth.uid + "/Item/date")
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllSales(callback: ReadSalesDataCallback) {
        dbSales.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val salesList = ArrayList<AddSales>()
                for (saleSnapshot in snapshot.children) {
                    val sale = saleSnapshot.getValue(AddSales::class.java)
                    if (sale != null) {
                        salesList.add(sale)
                    }
                }
                callback.readData(salesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DbManager", "Failed to read sales data", error.toException())
            }
        })
    }

    fun deleteAd(addNom: AddNom, listener: FinishWorkListener) {
        if (addNom.id == null || addNom.uid == null) return
        db.child(addNom.id).child(addNom.uid).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish(true)

        }
    }

    fun deleteSellAd(addSales: AddSales, onFinish: FinishWorkListener) {
        if (addSales.idItem != null) {
            dbSales.child(addSales.idItem!!).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onFinish.onFinish(true)
                } else {
                    onFinish.onFinish(false)
                }
            }
        } else {
            onFinish.onFinish(false)
        }
    }

    private fun readDataFromDb(query: Query, readDataCallBack: ReadDataCallBack?) {
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val addArray = ArrayList<AddNom>()
                for (item in snapshot.children) {
                    val ad = item.children.iterator().next().child(AD_NODE)
                        .getValue(AddNom::class.java)
                    if (ad != null) addArray.add(ad)
                }
                readDataCallBack?.readData(addArray)
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
        val saleReference = dbSales.orderByChild("uid").equalTo(uid)

        saleReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val sale = snapshot.getValue(AddSales::class.java)

                    if (sale?.id == itemId && sale.date == saleDate && sale.paymentMethod == paymentMethod) {
                        listener.onFinish(snapshot.key, sale)
                        return
                    }
                }

                listener.onFinish(null, null)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFinish(null, null)
            }
        })
    }

    fun updateSaleQuantityAndPrice(
        saleKey: String,
        newQuantity: String,
        newPrice: String,
        finishWorkListener: FinishWorkListener
    ) {
        val saleReference = dbSales.child(saleKey)

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

    interface ReadDataCallBack {
        fun readData(list: ArrayList<AddNom>)

    }

    interface ReadSalesDataCallback {
        fun readData(list: ArrayList<AddSales>)
    }

    interface FinishWorkListener {
        fun onFinish(isDone: Boolean)
    }


    companion object {
        const val AD_NODE = "Item"
        const val MAIN_NODE = "Nomenclature"
        const val SALE_NODE = "Sales"


    }

}