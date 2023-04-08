package com.stato.jewintage.db

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.stato.jewintage.data.AddNom

class DbManager {

    val db = Firebase.database.getReference("main")
    private val auth = Firebase.auth

    fun publishAdd(addNom: AddNom){
        if(auth.uid != null)db.child(addNom.id ?: "empty").child(auth.uid!!).child("Nomenclature").setValue(addNom)
    }

    fun readDataFromDb(){
        db.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children){
                    val add = item.children.iterator().next().child("Nomenclature").getValue(AddNom::class.java)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}