package com.stato.jewintage.util

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.fragment.app.Fragment
import com.stato.jewintage.EditItemAct
import com.stato.jewintage.R
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImagePicker {
    const val MAX_IMAGE_COUNT = 3
    private fun getOptions(imageCounter: Int): Options {
        val options = Options().apply {
            count = imageCounter
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
        }
        return options
    }

    fun getMultiImages(edAct: EditItemAct, imageCounter: Int) {
        edAct.addPixToActivity(R.id.placeHolder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    getMultiSelectImages(edAct, result.data)
                }
                else -> {}
            }
        }
    }

    fun addImages(edAct: EditItemAct, imageCounter: Int) {
        edAct.addPixToActivity(R.id.placeHolder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(edAct)
                    edAct.chooseImageFrag?.updateAdapter(result.data as ArrayList<Uri>, edAct)
                }
                else -> {}
            }
        }
    }
    fun getSingleImages(edAct: EditItemAct) {
        edAct.addPixToActivity(R.id.placeHolder, getOptions(1)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(edAct)
                    singleImage(edAct, result.data[0])
                }
                else -> {}
            }
        }
    }

    private fun openChooseImageFrag(edAct: EditItemAct){
        edAct.supportFragmentManager.beginTransaction().replace(R.id.placeHolder, edAct.chooseImageFrag!!).commit()
    }

    private fun closePixFrag(edAct: EditItemAct) {
        val fList = edAct.supportFragmentManager.fragments
        fList.forEach {
            if (it.isVisible) edAct.supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun getMultiSelectImages(edAct: EditItemAct, uris: List<Uri>) {
        if (uris.size > 1 && edAct.chooseImageFrag == null) {
            edAct.openChooseItemFrag(uris as ArrayList<Uri>)
        } else if (uris.size == 1 && edAct.chooseImageFrag == null) {
            CoroutineScope(Dispatchers.Main).launch {
                edAct.binding.progressBar.visibility = View.VISIBLE
                val bitMapArray = ImageManager.imageResize(uris, edAct) as ArrayList<Bitmap>
                edAct.binding.progressBar.visibility = View.GONE
                edAct.imageAdapter.update(bitMapArray)
                closePixFrag(edAct)
            }
        }
    }
}

private fun singleImage(edAct: EditItemAct, uri : Uri) {
    edAct.chooseImageFrag?.setSingleImage(uri, edAct.editImagePos)

}