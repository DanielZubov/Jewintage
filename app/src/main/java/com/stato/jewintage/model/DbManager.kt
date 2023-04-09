package com.stato.jewintage.model

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager {

    val db = Firebase.database.getReference("main")
    val auth = Firebase.auth

    fun publishAdd(addNom: AddNom) {
        if (auth.uid != null)
            db.child(addNom.id ?: "empty")
                .child(auth.uid!!)
                .child("Nomenclature")
            .setValue(addNom)
    }

    fun readDataFromDb(readDataCallBack: ReadDataCallBack?) {

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val addArray = ArrayList<AddNom>()
                for (item in snapshot.children) {
                    val ad = item.children.iterator().next().child("Nomenclature")
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

}