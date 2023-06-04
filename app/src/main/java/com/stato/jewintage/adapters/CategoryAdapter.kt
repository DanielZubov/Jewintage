package com.stato.jewintage.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.TypedArrayUtils.getString
import androidx.recyclerview.widget.RecyclerView
import com.stato.jewintage.R
import com.stato.jewintage.databinding.CategoryItemBinding
import com.stato.jewintage.model.Category
import com.stato.jewintage.viewmodel.FirebaseViewModel


@Suppress("DEPRECATION")
class CategoryAdapter(
    private var categories: List<Category>,
    private val listener: OnCommissionChangeListener,
    private val firebaseViewModel: FirebaseViewModel
) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    interface OnCommissionChangeListener {
        fun onCommissionChange(category: Category, commission: Float)
    }

    inner class CategoryViewHolder(val binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.cvCategoryItem.setOnClickListener { it ->
                val category = categories[adapterPosition]
                val inflater = LayoutInflater.from(it.context)
                val dialogView = inflater.inflate(R.layout.dialog_commission_category, null)
                val etNameCategory = dialogView.findViewById<EditText>(R.id.etCommission)
                etNameCategory.setText(category.commission.toString())
                val btnSubmitSave = dialogView.findViewById<Button>(R.id.btnSubmitSave)
                val tvTitle = dialogView.findViewById<TextView>(R.id.tvCategory)
                tvTitle.text = it.context.getString(R.string.edit_commission, category.name)

                val dialog = AlertDialog.Builder(it.context)
                    .setView(dialogView)
                    .create()

                btnSubmitSave.setOnClickListener {
                    val commission = etNameCategory.text.toString().toFloatOrNull()
                    if (commission != null && commission >= 0 && commission <= 100) {
                        category.commission = commission
                        listener.onCommissionChange(category, commission)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            it.context,
                            it.context.getString(R.string.warning_commission),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
            }



        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding =
            CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.deleteButton.setOnClickListener {
            firebaseViewModel.deleteCategoryItem(category, position)
        }
        holder.binding.categoryName.text = category.name
        holder.binding.commissionInput.text = category.commission.toString()
    }


    override fun getItemCount(): Int = categories.size
}

