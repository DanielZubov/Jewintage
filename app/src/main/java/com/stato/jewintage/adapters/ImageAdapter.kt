package com.stato.jewintage.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.stato.jewintage.R

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageHolder>() {
    val mainArray = ArrayList<Bitmap>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_adapter_item, parent, false)
        return ImageHolder(view)
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    class ImageHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        lateinit var imItem : ImageView
        fun setData(bitmap: Bitmap) {
            imItem = itemView.findViewById(R.id.imItem)
            imItem.setImageBitmap(bitmap)
            imItem.scaleType = ImageView.ScaleType.CENTER_CROP
        }

    }

    fun update(newList: ArrayList<Bitmap>) {
        val diffCallback = DiffUtilHelperBitmap(mainArray, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        mainArray.clear()
        mainArray.addAll(newList)

        diffResult.dispatchUpdatesTo(this)
    }


}