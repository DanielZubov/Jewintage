package com.stato.jewintage.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stato.jewintage.databinding.GroupHeaderBinding
import com.stato.jewintage.model.DbManager

class NomGroupAdapter(
    private val categoryClickListener: OnCategoryClickListener,
    private val dbManager: DbManager
) : RecyclerView.Adapter<NomGroupAdapter.NomGroupViewHolder>() {

    private val categoryList = ArrayList<String>()


    @SuppressLint("NotifyDataSetChanged")
    fun setData(newList: List<String>) {
        categoryList.clear()
        categoryList.addAll(sortCategories(newList))
        notifyDataSetChanged()
    }


    private fun sortCategories(categoryList: List<String>): List<String> {
        val categoryListWithoutAll = categoryList.filterNot { it == "Все" }
        val sortedCategories = categoryListWithoutAll.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
        return listOf("Все") + sortedCategories
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NomGroupViewHolder {
        val binding = GroupHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NomGroupViewHolder(binding, categoryClickListener, categoryList, dbManager)
    }

    override fun getItemCount(): Int = categoryList.size

    override fun onBindViewHolder(holder: NomGroupViewHolder, position: Int) {
        holder.bind(position)
    }

    class NomGroupViewHolder(
        private val binding: GroupHeaderBinding,
        private val dateClickListener: OnCategoryClickListener,
        private val categoryList: ArrayList<String>,
        private val dbManager: DbManager
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) = with(binding) {

            val category = categoryList[position]
            tvGroupHeader.text = category

            dbManager.calculateTotalSalesByCategory(category) { total ->
                tvGroupSum.text = "$total шт."
            }

            cvGroupItem.setOnClickListener {
                dateClickListener.onCategoryClick(category)
            }
        }
    }

    interface OnCategoryClickListener {
        fun onCategoryClick(category: String)
    }
}
