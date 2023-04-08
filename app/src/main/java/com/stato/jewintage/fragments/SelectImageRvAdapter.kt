package com.stato.jewintage.fragments

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stato.jewintage.EditItemAct
import com.stato.jewintage.R
import com.stato.jewintage.databinding.SelectImageFragmentItemBinding
import com.stato.jewintage.util.AdapterCallback
import com.stato.jewintage.util.ImageManager
import com.stato.jewintage.util.ImagePicker
import com.stato.jewintage.util.ItemMoveCallBack

class SelectImageRvAdapter(val adapterCallback: AdapterCallback) : RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(), ItemMoveCallBack.ItemMoveAdapter {
    val mainArray = ArrayList<Bitmap>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val binding = SelectImageFragmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHolder(binding, parent.context, this)
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onMove(startPos: Int, targetPos: Int) {

        val targetItem = mainArray[targetPos]
        mainArray[targetPos] = mainArray[startPos]
        mainArray[startPos] = targetItem
        notifyItemMoved(startPos, targetPos)
    }

    override fun onClear() {
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }



    class ImageHolder(private val binding: SelectImageFragmentItemBinding, private val context : Context, val adapter: SelectImageRvAdapter) : RecyclerView.ViewHolder(binding.root) {

        fun setData(bitMap : Bitmap) {

            binding.imageLayout.setOnClickListener {
                ImagePicker.getImages(context as EditItemAct, 1, ImagePicker.REQUEST_CODE_GET_SINGLE_IMAGE)
                context.editImagePos = adapterPosition
            }

            binding.btnDeItem.setOnClickListener {
                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                for (n in 0 until adapter.mainArray.size)
                    adapter.notifyItemChanged(n)
                adapter.adapterCallback.onItemDel()
            }

            binding.tvTitle.text = context.resources.getStringArray(R.array.tittle_image)[adapterPosition]
            ImageManager.chooseScaleType(binding.imageContent, bitMap)
            binding.imageContent.setImageBitmap(bitMap)
        }
    }

    fun updateAdapter(newList : List<Bitmap>, needClear : Boolean){
        if (needClear)mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }


}

