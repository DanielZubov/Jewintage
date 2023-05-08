package com.stato.jewintage.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.stato.jewintage.adapters.ImageAdapter
import com.stato.jewintage.model.AddCost
import com.stato.jewintage.model.AddNom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ImageManager {
    private const val MAX_IMAGE_SIZE = 1000
    private const val WIDTH = 0
    private const val HEIGHT = 1

    private fun getImageSize(uri: Uri, act : Activity) : List<Int> {
        val inputStream = act.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        return listOf(options.outWidth, options.outHeight)
    }



    fun chooseScaleType(im: ImageView, bitMap: Bitmap){
        if (bitMap.width > bitMap.height){
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    suspend fun imageResize(uris: List<Uri>, act: Activity): List<Bitmap> = withContext(Dispatchers.IO) {
        val tempList = ArrayList<List<Int>>()
        val bitmapList = ArrayList<Bitmap>()

        for (n in uris.indices) {

            val size = getImageSize(uris[n], act)
            val imageRatio = size[WIDTH].toFloat() / size[HEIGHT].toFloat()

            if (imageRatio > 1) {
                if (size[WIDTH] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt()))
                } else {
                    tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                }
            } else {
                if (size[HEIGHT] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))
                } else {
                    tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                }
            }
        }
        for (i in uris.indices) {
            kotlin.runCatching {
                bitmapList.add(
                    Picasso.get()
                        .load(uris[i])
                        .resize(
                            tempList[i][WIDTH],
                            tempList[i][HEIGHT]
                        ).get()
                )
            }
        }
        return@withContext bitmapList
    }

    private suspend fun getBitmapForUris(uris: List<String?>): List<Bitmap> = withContext(Dispatchers.IO) {

        val bitmapList = ArrayList<Bitmap>()
        for (i in uris.indices) {
            kotlin.runCatching {
                bitmapList.add(
                    Picasso.get()
                        .load(uris[i])
                        .get()
                )

            }
        }
        return@withContext bitmapList
    }

    fun fillImageArray(ad: AddNom, adapter: ImageAdapter){
        val listUris = listOf(ad.mainImage, ad.image2, ad.image3,)
        CoroutineScope(Dispatchers.Main).launch {
            val bitMapList = getBitmapForUris(listUris)
            adapter.update(bitMapList as ArrayList<Bitmap>)
        }
    }
    fun fillImageCostArray(cost: AddCost, adapter: ImageAdapter){
        val listUris = listOf(cost.mainImage, cost.image2, cost.image3,)
        CoroutineScope(Dispatchers.Main).launch {
            val bitMapList = getBitmapForUris(listUris)
            adapter.update(bitMapList as ArrayList<Bitmap>)
        }
    }
    fun fillImageSellArray(imageUrls: List<String?>, adapter: ImageAdapter) {
        CoroutineScope(Dispatchers.Main).launch {
            val bitMapList = getBitmapForUris(imageUrls)
            adapter.update(bitMapList as ArrayList<Bitmap>)
        }
    }

}