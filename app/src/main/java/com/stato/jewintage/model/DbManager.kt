package com.stato.jewintage.model

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class DbManager {

    val db = Firebase.database.getReference(MAIN_NODE)
    val dbStorage = Firebase.storage.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun publishAdd(addNom: AddNom, finishWorkListener: FinishWorkListener) {
        if (auth.uid != null)
            db.child(addNom.id ?: "empty")
                .child(auth.uid!!)
                .child(AD_NODE)
            .setValue(addNom).addOnCompleteListener {
                    finishWorkListener.onFinish(it.isSuccessful)
                }
    }

    fun getMyAds(readDataCallBack: ReadDataCallBack?){
        val query = db.orderByChild(auth.uid + "/Item/uid").equalTo(auth.uid)
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllAds(readDataCallBack: ReadDataCallBack?){
        val query = db.orderByChild(auth.uid + "/Item/date")
        readDataFromDb(query, readDataCallBack)
    }

    fun deleteAd(addNom: AddNom, listener: FinishWorkListener){
        if (addNom.id == null || addNom.uid == null) return
        db.child(addNom.id).child(addNom.uid).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish(true)

        }
    }

    private fun readDataFromDb(query : Query, readDataCallBack: ReadDataCallBack?) {
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
    interface ReadDataCallBack {
        fun readData(list : ArrayList<AddNom>)

    }

    interface FinishWorkListener{
        fun onFinish(isDone: Boolean)
    }

    companion object{
        const val AD_NODE = "Item"
        const val MAIN_NODE = "Nomenclature"

    }

}