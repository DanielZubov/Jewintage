package com.stato.jewintage.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.databinding.AddNomListItemBinding

class AddRcAdapter(val auth : FirebaseAuth) : RecyclerView.Adapter<AddRcAdapter.AdHolder>() {
    val addArray = ArrayList<AddNom>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder {
        val binding = AddNomListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdHolder(binding, auth)
    }

    override fun getItemCount(): Int {
        return addArray.size
    }

    override fun onBindViewHolder(holder: AdHolder, position: Int) {
        holder.setData(addArray[position])
    }

    fun updateAdapter(newList : List<AddNom>){
        addArray.clear()
        addArray.addAll(newList)
        notifyDataSetChanged()
    }

    class AdHolder(private val binding: AddNomListItemBinding, val auth: FirebaseAuth) : RecyclerView.ViewHolder(binding.root) {
        private val admin = "a4WJjtVL6UXzRz8v7SKPajIuBYN2"

        fun setData(addNom: AddNom){
            binding.apply {
                tvNumItemCat.text = addNom.category
                tvNumItemDescription.text = addNom.description
                tvNumItemPrice.text = addNom.price
                tvNumItemQuant.text = addNom.quantity
            }
            showItems(isOwner(addNom))
        }

        private fun isOwner(addNom: AddNom): Boolean{
            return auth.uid == admin || addNom.uid == auth.uid
        }

        private fun showItems(isOwner: Boolean){
            if (isOwner){
                binding.itemNum.visibility = View.VISIBLE
            } else {
                binding.itemNum.visibility = View.GONE
            }
        }

    }

}