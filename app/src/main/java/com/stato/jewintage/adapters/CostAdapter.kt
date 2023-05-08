package com.stato.jewintage.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.stato.jewintage.EditItemCost
import com.stato.jewintage.MainActivity
import com.stato.jewintage.R
import com.stato.jewintage.databinding.AddSaleListItemBinding
import com.stato.jewintage.model.AddCost
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CostAdapter(
    private val act: MainActivity,
    private val descriptionClickListener: OnDescriptionClickListener
) : RecyclerView.Adapter<CostAdapter.CostViewHolder>()  {
    private val fullCostList : ArrayList<AddCost> = ArrayList()
    private var costList = ArrayList<AddCost>()
        set(value) {
            field = value
            costList.clear()
            costList.addAll(value)
        }

    fun setData(newList: List<AddCost>) {
        val sortedList = sortByDateDescending(newList)
        val diffResult = DiffUtil.calculateDiff(DiffUtilCost(costList, sortedList))
        costList.clear()
        costList.addAll(sortedList)
        fullCostList.clear()
        fullCostList.addAll(sortedList)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun sortByDateDescending(list: List<AddCost>): List<AddCost> {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        return list.sortedWith { item1, item2 ->
            val date1 = format.parse(item1.date.toString()) ?: Date()
            val date2 = format.parse(item2.date.toString()) ?: Date()
            date2.compareTo(date1)
        }
    }

    private fun updateCosts(newSalesList: List<AddCost>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCost(costList, newSalesList))
        costList.clear()
        costList.addAll(newSalesList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CostViewHolder {
        val binding =
            AddSaleListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CostViewHolder(binding, act, descriptionClickListener, costList)

    }

    fun filter(query: String) {
        val filteredList = ArrayList<AddCost>()
        val lowercasedQuery = query.lowercase()

        for (item in fullCostList) {
            val category = item.category?.lowercase()

            if (category?.contains(lowercasedQuery) == true) {
                filteredList.add(item)
            }
        }
        updateCosts(filteredList)
    }

    override fun getItemCount(): Int = costList.size

    override fun onBindViewHolder(holder: CostViewHolder, position: Int) {
        holder.bind(position)
    }
    class CostViewHolder(
        private val binding: AddSaleListItemBinding,
        private val act: MainActivity,
        private val descriptionClickListener: OnDescriptionClickListener,
        private val costList: ArrayList<AddCost>
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(position: Int) = with(binding) {
            val cost = costList[position]
            val newPrice = (cost.price!!.toInt() * cost.quantity!!.toInt()).toString()
            tvSaleItemCat.text = cost.category
            tvSaleItemDescription.text = cost.date
            "₾ $newPrice".also { tvSaleItemSum.text = it }
            "${cost.quantity} шт.".also { tvSaleItemQuant.text = it }
            val mainImage = cost.mainImage
            if (!mainImage.isNullOrEmpty()) {
                Picasso.get().load(mainImage).into(ivSaleItem)
            } else {
                val context = binding.root.context
                val defaultImage = ContextCompat.getDrawable(context, R.drawable.image_item)
                ivSaleItem.setImageDrawable(defaultImage)
            }
            mainOnClick(cost)
            showItems(isOwner(cost))
        }

        private fun mainOnClick(cost: AddCost) = with(binding) {
            cvSlItem.setOnClickListener {
                descriptionClickListener.onDescriptionClick(cost)
            }
            ibEditSlItem.setOnClickListener(onEditClick(cost))

            ibDeleteSlItem.setOnClickListener {
                act.onDeleteCostItem(cost)
            }
        }

        private fun onEditClick(cost: AddCost): View.OnClickListener {
            return View.OnClickListener {
                val i = Intent(act, EditItemCost::class.java).apply {
                    putExtra(MainActivity.EDIT_STATE, true)
                    putExtra(MainActivity.ADS_DATA, cost)

                }
                act.startActivity(i)
            }
        }


        private fun isOwner(cost: AddCost): Boolean {
            return cost.uid == act.auth.uid
        }

        private fun showItems(isOwner: Boolean) {
            if (isOwner) {
                binding.ibDeleteSlItem.visibility = View.VISIBLE
                binding.ibEditSlItem.visibility = View.VISIBLE
            } else {
                binding.ibDeleteSlItem.visibility = View.GONE
                binding.ibEditSlItem.visibility = View.GONE
            }
        }
    }

    interface DeleteItemListener {
        fun onDeleteCostItem(cost: AddCost)
    }


    interface OnDescriptionClickListener {
        fun onDescriptionClick(cost: AddCost)
    }

}