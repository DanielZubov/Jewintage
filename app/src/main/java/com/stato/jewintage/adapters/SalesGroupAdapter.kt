package com.stato.jewintage.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stato.jewintage.databinding.GroupHeaderBinding

class SalesGroupAdapter(
    private val dateClickListener: OnDateClickListener
) : RecyclerView.Adapter<SalesGroupAdapter.SalesGroupViewHolder>() {

    private val dateList = ArrayList<String>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newList: List<String>) {
        dateList.clear()
        dateList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesGroupViewHolder {
        val binding = GroupHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SalesGroupViewHolder(binding, dateClickListener, dateList)
    }

    override fun getItemCount(): Int = dateList.size

    override fun onBindViewHolder(holder: SalesGroupViewHolder, position: Int) {
        holder.bind(position)
    }

    class SalesGroupViewHolder(
        private val binding: GroupHeaderBinding,
        private val dateClickListener: OnDateClickListener,
        private val dateList: ArrayList<String>
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) = with(binding) {
            val date = dateList[position]
            tvGroupHeader.text = date

            cvGroupItem.setOnClickListener {
                dateClickListener.onDateClick(date)
            }
        }
    }

    interface OnDateClickListener {
        fun onDateClick(date: String)
    }
}
