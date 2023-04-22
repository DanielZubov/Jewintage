package com.stato.jewintage.fragments

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.stato.jewintage.DescriptionActivity
import com.stato.jewintage.EditItemAct
import com.stato.jewintage.MainActivity
import com.stato.jewintage.constance.MainConst
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
            tvPaymentMethod.text = sale.paymentMethod
            tvSaleItemCat.text = sale.category
            tvSaleItemDescription.text = sale.description
            "₾ ${sale.price}".also { tvSaleItemSum.text = it }
            "${sale.soldQuantity} шт.".also { tvSaleItemQuant.text = it }
            Picasso.get().load(sale.mainImage).into(ivSaleItem)
            mainOnClick(sale)
            showItems(isOwner(sale))
        }

        private fun mainOnClick(sale: AddSales) = with(binding){
            cvSlItem.setOnClickListener {
                val i = Intent(binding.root.context, DescriptionActivity::class.java)
                i.putExtra(MainConst.AD, sale)
                binding.root.context.startActivity(i)
            }
            ibEditSlItem.setOnClickListener (onClickEdit(sale))
            ibDeleteSlItem.setOnClickListener {
                Log.d("mainOnClick", "Delete button clicked, calling onDeleteSellItem")
                act.onDeleteSellItem(sale)
            }
        }

        private fun onClickEdit(sale: AddSales) : View.OnClickListener{
            return View.OnClickListener {
                val i = Intent(act, EditItemAct::class.java).apply {
                    putExtra(MainActivity.EDIT_STATE, true)
                    putExtra(MainActivity.ADS_DATA, sale)

                }
                act.startActivity(i)
            }
        }

        private fun isOwner(sale: AddSales): Boolean{
            return sale.uid == act.auth.uid
        }

        private fun showItems(isOwner: Boolean){
            if (isOwner){
                binding.ibDeleteSlItem.visibility = View.VISIBLE
                binding.ibEditSlItem.visibility = View.VISIBLE
            } else {
                binding.ibDeleteSlItem.visibility = View.GONE
                binding.ibEditSlItem.visibility = View.GONE
            }
        }
    }
    interface DeleteItemListener{
        fun onDeleteSellItem(sale: AddSales)
    }
}
