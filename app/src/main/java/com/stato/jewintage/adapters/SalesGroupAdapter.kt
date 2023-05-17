package com.stato.jewintage.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stato.jewintage.databinding.GroupHeaderBinding
import com.stato.jewintage.model.DbManager
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale

class SalesGroupAdapter(
    private val dateClickListener: OnDateClickListener,
    private val dbManager: DbManager
) : RecyclerView.Adapter<SalesGroupAdapter.SalesGroupViewHolder>() {

    private val dateList = ArrayList<String>()


    @SuppressLint("NotifyDataSetChanged")
    fun setData(newList: List<String>) {
        dateList.clear()
        dateList.addAll(sortDates(newList))
        notifyDataSetChanged()
    }


    private fun sortDates(dateList: List<String>): List<String> {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val dateListWithoutAll = dateList.filterNot { it == "Все" }
        val sortedDates = dateListWithoutAll
            .mapNotNull { dateStr ->
                try {
                    dateFormat.parse(dateStr)?.let { date -> dateStr to date }
                } catch (e: Exception) {
                    null
                }
            }
            .sortedByDescending { (_, date) -> date }
            .map { (dateStr, _) -> dateStr }
        return listOf("Все") + sortedDates
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesGroupViewHolder {
        val binding = GroupHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SalesGroupViewHolder(binding, dateClickListener, dateList, dbManager)
    }

    override fun getItemCount(): Int = dateList.size

    override fun onBindViewHolder(holder: SalesGroupViewHolder, position: Int) {
        holder.bind(position)
    }

    class SalesGroupViewHolder(
        private val binding: GroupHeaderBinding,
        private val dateClickListener: OnDateClickListener,
        private val dateList: ArrayList<String>,
        private val dbManager: DbManager
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) = with(binding) {
            val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
                decimalSeparator = '.'
            }
            val df = DecimalFormat("#.##", symbols)
            val date = dateList[position]
            tvGroupHeader.text = date

            dbManager.calculateTotalSalesByDate(date) { total ->
                val sum = df.format(total)
                tvGroupSum.text = "₾$sum"
            }

            cvGroupItem.setOnClickListener {
                dateClickListener.onDateClick(date)
            }
        }
    }

    interface OnDateClickListener {
        fun onDateClick(date: String)
    }
}
