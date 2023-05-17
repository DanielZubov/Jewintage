package com.stato.jewintage.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.stato.jewintage.MainActivity
import com.stato.jewintage.R
import com.stato.jewintage.databinding.AddSaleListItemBinding
import com.stato.jewintage.model.AddSales
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalesAdapter(
    private val act: MainActivity,
    private val editClickListener: OnEditClickListener,
    private val descriptionClickListener: OnDescriptionClickListener
) : RecyclerView.Adapter<SalesAdapter.SalesViewHolder>() {

    private val fullSalesList : ArrayList<AddSales> = ArrayList()
    private var salesList = ArrayList<AddSales>()
        set(value) {
            field = value
            salesList.clear()
            salesList.addAll(value)
        }

    fun setData(newList: List<AddSales>) {
        val sortedList = sortByDateDescending(newList)
        val diffResult = DiffUtil.calculateDiff(DiffUtilSales(salesList, sortedList))
        salesList.clear()
        salesList.addAll(sortedList)
        fullSalesList.clear()
        fullSalesList.addAll(sortedList)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun sortByDateDescending(list: List<AddSales>): List<AddSales> {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        return list.sortedWith { item1, item2 ->
            val date1 = format.parse(item1.date.toString()) ?: Date()
            val date2 = format.parse(item2.date.toString()) ?: Date()
            date2.compareTo(date1)
        }
    }
    private fun updateSales(newSalesList: List<AddSales>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilSales(salesList, newSalesList))
        salesList.clear()
        salesList.addAll(newSalesList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        val binding =
            AddSaleListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SalesViewHolder(binding, act, editClickListener, descriptionClickListener, salesList)

    }

    fun filter(query: String) {
        val filteredList = ArrayList<AddSales>()
        val lowercasedQuery = query.lowercase()

        for (item in fullSalesList) {
            val category = item.category?.lowercase()

            if (category?.contains(lowercasedQuery) == true) {
                filteredList.add(item)
            }
        }
        updateSales(filteredList)
    }

    override fun getItemCount(): Int = salesList.size

    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        holder.bind(position)
    }
    class SalesViewHolder(
        private val binding: AddSaleListItemBinding,
        private val act: MainActivity,
        private val editClickListener: OnEditClickListener,
        private val descriptionClickListener: OnDescriptionClickListener,
        private val salesList: ArrayList<AddSales>
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(position: Int) = with(binding) {
            val sale = salesList[position]
            tvPaymentMethod.text = sale.paymentMethod
            tvSaleItemCat.text = sale.category
            tvSaleItemDescription.text = sale.date
            "₾ ${sale.price}".also { tvSaleItemSum.text = it }
            "${sale.soldQuantity} шт.".also { tvSaleItemQuant.text = it }
            // Добавлена проверка перед вызовом Picasso.load()
            val mainImage = sale.mainImage
            if (!mainImage.isNullOrEmpty() && mainImage != "empty") {
                Picasso.get().load(mainImage).into(ivSaleItem)
            } else {
                val context = binding.root.context
                val defaultImage = ContextCompat.getDrawable(context, R.drawable.no_image_available)
                ivSaleItem.setImageDrawable(defaultImage)
            }
            mainOnClick(sale)
            showItems(isOwner(sale))
        }

        private fun mainOnClick(sale: AddSales) = with(binding) {
            cvSlItem.setOnClickListener {
                descriptionClickListener.onDescriptionClick(sale)
            }
            ibEditSlItem.setOnClickListener {
                editClickListener.onEditClick(sale)
            }
            ibDeleteSlItem.setOnClickListener {
                act.onDeleteSellItem(sale)
            }
        }


        private fun isOwner(sale: AddSales): Boolean {
            return sale.uid == act.auth.uid
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
        fun onDeleteSellItem(sale: AddSales)
    }

    interface OnEditClickListener {
        fun onEditClick(sale: AddSales)
    }

    interface OnDescriptionClickListener {
        fun onDescriptionClick(sale: AddSales)
    }

}
