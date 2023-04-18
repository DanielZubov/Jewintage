package com.stato.jewintage.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.stato.jewintage.MainActivity
import com.stato.jewintage.databinding.AddSaleListItemBinding
import com.stato.jewintage.model.AddSales

class SalesAdapter(private val act: MainActivity) : RecyclerView.Adapter<SalesAdapter.SalesViewHolder>() {
    private val salesList = ArrayList<AddSales>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        val binding = AddSaleListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SalesViewHolder(binding, act)
    }

    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        holder.bind(salesList[position])
    }

    override fun getItemCount(): Int = salesList.size

    fun updateSales(newSalesList: List<AddSales>) {
        salesList.clear()
        salesList.addAll(newSalesList)
        notifyDataSetChanged()
    }

    class SalesViewHolder(private val binding: AddSaleListItemBinding, private val act: MainActivity) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sale: AddSales) = with(binding) {
            tvSaleItemCat.text = sale.category
            tvSaleItemDescription.text = sale.description
            "₾ ${sale.price}".also { tvSaleItemSum.text = it }
            "${sale.soldQuantity} шт.".also { tvSaleItemQuant.text = it }
            Picasso.get().load(sale.mainImage).into(ivSaleItem)
        }
    }
}
