package com.stato.jewintage.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.stato.jewintage.DescriptionActivity
import com.stato.jewintage.EditItemAct
import com.stato.jewintage.MainActivity
import com.stato.jewintage.R
import com.stato.jewintage.constance.MainConst
import com.stato.jewintage.databinding.AddNomListItemBinding
import com.stato.jewintage.model.AddNom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddRcAdapter(
    private val act: MainActivity,
    private val sellButtonClickListener: SellButtonClickListener
) : RecyclerView.Adapter<AddRcAdapter.AdHolder>() {
    private val fullAddArray: ArrayList<AddNom> = ArrayList()

    private var addArray = ArrayList<AddNom>()
        set(value) {
            field = value
            fullAddArray.clear()
            fullAddArray.addAll(value)
        }

    fun setData(newList: List<AddNom>) {
        val sortedList = sortByDateDescending(newList)
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(addArray, sortedList))
        addArray.clear()
        addArray.addAll(sortedList)
        fullAddArray.clear()
        fullAddArray.addAll(sortedList)
        diffResult.dispatchUpdatesTo(this)
    }
    private fun sortByDateDescending(list: List<AddNom>): List<AddNom> {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        return list.sortedWith { item1, item2 ->
            val date1 = format.parse(item1.date.toString()) ?: Date()
            val date2 = format.parse(item2.date.toString()) ?: Date()
            date2.compareTo(date1)
        }
    }


    private fun updateAdapter(newList: List<AddNom>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(addArray, newList))
        addArray.clear()
        addArray.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder {
        val binding =
            AddNomListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdHolder(binding, act, sellButtonClickListener, addArray)
    }


    fun filter(query: String) {
        val filteredList = ArrayList<AddNom>()
        val lowercasedQuery = query.lowercase()

        for (item in fullAddArray) {
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
        holder.setData(position)
    }


    class AdHolder(
        private val binding: AddNomListItemBinding,
        private val act: MainActivity,
        private val sellButtonClickListener: SellButtonClickListener,
        private val addArray: ArrayList<AddNom>
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setData(position: Int) = with(binding) {
            val addNom = addArray[position]
            if (addNom.quantity == "0") {
                cvItem.visibility = View.GONE
            } else {
                cvItem.visibility = View.VISIBLE
            }
            tvNumItemCat.text = addNom.category
            tvNumItemDescription.text = addNom.description
            "₾ ${addNom.price}".also { tvNumItemPrice.text = it }
            "${addNom.quantity} шт.".also { tvNumItemQuant.text = it }

            val mainImage = addNom.mainImage
            if (!mainImage.isNullOrEmpty() && mainImage != "empty") {
                Picasso.get().load(mainImage).into(ivNomItem)
            } else {
                val context = binding.root.context
                val defaultImage = ContextCompat.getDrawable(context, R.drawable.no_image_available)
                ivNomItem.setImageDrawable(defaultImage)
            }

            showItems(isOwner(addNom))
            mainOnClick(addNom)
            ibSellItem.setOnClickListener { onSellButtonClick(addNom) }
        }


        private fun onSellButtonClick(addNom: AddNom) {
            sellButtonClickListener.onSellButtonClick(addNom)
        }

        private fun mainOnClick(addNom: AddNom) = with(binding) {
            cvItem.setOnClickListener {
                val i = Intent(binding.root.context, DescriptionActivity::class.java)
                i.putExtra(MainConst.AD, addNom)
                binding.root.context.startActivity(i)
            }
            ibEditItem.setOnClickListener(onClickEdit(addNom))
            ibDeleteItem.setOnClickListener {
                act.onDeleteItem(addNom)
            }
        }

        private fun onClickEdit(addNom: AddNom): View.OnClickListener {
            return View.OnClickListener {
                val i = Intent(act, EditItemAct::class.java).apply {
                    putExtra(MainActivity.EDIT_STATE, true)
                    putExtra(MainActivity.ADS_DATA, addNom)

                }
                act.startActivity(i)
            }
        }

        private fun isOwner(addNom: AddNom): Boolean {
            return addNom.uid == act.auth.uid
        }

        private fun showItems(isOwner: Boolean) {
            if (isOwner) {
                binding.ibDeleteItem.visibility = View.VISIBLE
                binding.ibEditItem.visibility = View.VISIBLE
                binding.ibSellItem.visibility = View.VISIBLE
            } else {
                binding.ibDeleteItem.visibility = View.GONE
                binding.ibEditItem.visibility = View.GONE
                binding.ibSellItem.visibility = View.GONE
            }
        }


    }

    interface DeleteItemListener {
        fun onDeleteItem(addNom: AddNom)
    }

    interface SellButtonClickListener {
        fun onSellButtonClick(addNom: AddNom)
    }


}