package com.stato.jewintage.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.stato.jewintage.R

class CategoryFilterAdapter(private val categories: Array<String>, private val onCategoryChecked: (String, Boolean) -> Unit) :
    RecyclerView.Adapter<CategoryFilterAdapter.CategoryViewHolder>() {

    val checkedCategories = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_filter_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.category_checkbox)

        fun bind(category: String) {
            checkBox.text = category
            checkBox.isChecked = checkedCategories.contains(category)

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkedCategories.add(category)
                } else {
                    checkedCategories.remove(category)
                }
                onCategoryChecked(category, isChecked)
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun resetCheckedCategories() {
        checkedCategories.clear()
        notifyDataSetChanged()
    }

}
