package com.stato.jewintage.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.stato.jewintage.EditItemAct
import com.stato.jewintage.MainActivity
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.databinding.AddNomListItemBinding

class AddRcAdapter(val act : MainActivity) : RecyclerView.Adapter<AddRcAdapter.AdHolder>() {
    val addArray = ArrayList<AddNom>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder {
        val binding = AddNomListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdHolder(binding, act)
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

    class AdHolder(private val binding: AddNomListItemBinding, val act : MainActivity) : RecyclerView.ViewHolder(binding.root) {

        fun setData(addNom: AddNom) = with(binding) {
            tvNumItemCat.text = addNom.category
            tvNumItemDescription.text = addNom.description
            tvNumItemPrice.text = addNom.price
            tvNumItemQuant.text = addNom.quantity
//            showItems(isOwner(addNom))
            cvItem.setOnClickListener (onClickEdit(addNom))
        }

        private fun onClickEdit(addNom: AddNom) : View.OnClickListener{
            return View.OnClickListener {
                val i = Intent(act, EditItemAct::class.java).apply {
                    putExtra(MainActivity.EDIT_STATE, true)
                    putExtra(MainActivity.ADS_DATA, addNom)

                }
                act.startActivity(i)
            }
        }

        private fun isOwner(addNom: AddNom): Boolean{
            return addNom.uid == act.auth.uid
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