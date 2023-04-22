package com.stato.jewintage.adapters

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.stato.jewintage.DescriptionActivity
import com.stato.jewintage.EditItemAct
import com.stato.jewintage.MainActivity
import com.stato.jewintage.constance.MainConst
import com.stato.jewintage.databinding.AddNomListItemBinding
import com.stato.jewintage.model.AddNom
import java.util.Locale

class AddRcAdapter(val act: MainActivity, private val sellButtonClickListener: SellButtonClickListener) : RecyclerView.Adapter<AddRcAdapter.AdHolder>() {
    val addArray = ArrayList<AddNom>()



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder {
        val binding = AddNomListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdHolder(binding, act, sellButtonClickListener)
    }


    fun filter(query: String) {
        val filteredList = ArrayList<AddNom>()
        val lowercasedQuery = query.lowercase()

        for (item in addArray) {
            val description = item.description?.lowercase()
            val category = item.category?.lowercase()

            if (description?.contains(lowercasedQuery) == true || category?.contains(lowercasedQuery) == true) {
                filteredList.add(item)
            }
        }
        updateAdapter(filteredList)
    }


    override fun getItemCount(): Int {
        return addArray.size
    }

    override fun onBindViewHolder(holder: AdHolder, position: Int) {
        holder.setData(addArray[position])
    }

    fun updateAdapter(newList : List<AddNom>){
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(addArray, newList))
        diffResult.dispatchUpdatesTo(this)
        addArray.clear()
        addArray.addAll(newList)
    }

    class AdHolder(private val binding: AddNomListItemBinding, private val act: MainActivity, private val sellButtonClickListener: SellButtonClickListener) : RecyclerView.ViewHolder(binding.root) {

        fun setData(addNom: AddNom) = with(binding) {
            if (addNom.quantity == "0") {
                cvItem.setCardBackgroundColor(Color.LTGRAY)
                ibSellItem.visibility = View.GONE
            }
            tvNumItemCat.text = addNom.category
            tvNumItemDescription.text = addNom.description
            "₾ ${addNom.price}".also { tvNumItemPrice.text = it }
            "${addNom.quantity} шт.".also { tvNumItemQuant.text = it }
            Picasso.get().load(addNom.mainImage).into(ivNomItem)
            showItems(isOwner(addNom))
            mainOnClick(addNom)
            ibSellItem.setOnClickListener { onSellButtonClick(addNom) }
        }
        private fun onSellButtonClick(addNom: AddNom) {
            sellButtonClickListener.onSellButtonClick(addNom)
        }

        private fun mainOnClick(addNom: AddNom) = with(binding){
            cvItem.setOnClickListener {
                val i = Intent(binding.root.context, DescriptionActivity::class.java)
                i.putExtra(MainConst.AD, addNom)
                binding.root.context.startActivity(i)
            }
            ibEditItem.setOnClickListener (onClickEdit(addNom))
            ibDeleteItem.setOnClickListener {
                act.onDeleteItem(addNom)
            }
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
                binding.ibDeleteItem.visibility = View.VISIBLE
                binding.ibEditItem.visibility = View.VISIBLE
            } else {
                binding.ibDeleteItem.visibility = View.GONE
                binding.ibEditItem.visibility = View.GONE
            }
        }


    }
    interface DeleteItemListener{
        fun onDeleteItem(addNom: AddNom)
    }
    interface SellButtonClickListener {
        fun onSellButtonClick(addNom: AddNom)
    }



}