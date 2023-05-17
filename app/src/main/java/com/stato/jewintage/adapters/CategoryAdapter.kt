package com.stato.jewintage.adapters

import android.annotation.SuppressLint
import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
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
            binding.cvCategoryItem.setOnClickListener {
                val category = categories[adapterPosition]
                val builder = AlertDialog.Builder(it.context)
                builder.setTitle("Enter commission for ${category.name}")

                val input = EditText(it.context)
                input.inputType = InputType.TYPE_CLASS_NUMBER
                input.setText(category.commission.toString())
                builder.setView(input)

                builder.setPositiveButton("OK") { dialog, _ ->
                    val commission = input.text.toString().toFloatOrNull()
                    if (commission != null && commission >= 0 && commission <= 100) {
                        category.commission = commission
                        listener.onCommissionChange(category, commission)
                    } else {
                        Toast.makeText(
                            it.context,
                            "Please enter a valid commission between 0 and 100",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    dialog.dismiss()
                }

                builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

                builder.show()
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

