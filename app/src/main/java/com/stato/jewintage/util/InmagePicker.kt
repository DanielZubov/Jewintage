package com.stato.jewintage.util

import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.stato.jewintage.EditItemAct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImagePicker {
    const val MAX_IMAGE_COUNT = 3
    const val REQUEST_CODE_GET_IMAGES = 999
    const val REQUEST_CODE_GET_SINGLE_IMAGE = 998
    fun getImages(context: AppCompatActivity, imageCounter: Int, rCode: Int) {
        val options = Options.init()
            .setRequestCode(rCode)
            .setCount(imageCounter)
            .setFrontfacing(false)
            .setSpanCount(4)
            .setMode(Options.Mode.Picture)
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)
            .setPath("/pix/images")

        Pix.start(context, options)
    }

    fun showSelectedImages(resultCode: Int, requestCode: Int, data: Intent?, edAct: EditItemAct) {

        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == REQUEST_CODE_GET_IMAGES) {
            if (data != null) {
                val returnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                if (returnValues?.size!! > 1 && edAct.chooseImageFrag == null) {
                    edAct.openChooseItemFrag(returnValues)
                } else if (returnValues.size == 1 && edAct.chooseImageFrag == null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        edAct.binding.progressBar.visibility = View.VISIBLE
                        val bitMapArray =
                            ImageManager.imageResize(returnValues) as ArrayList<Bitmap>
                        edAct.binding.progressBar.visibility = View.GONE
                        edAct.imageAdapter.update(bitMapArray)
                    }


                } else if (edAct.chooseImageFrag != null) {

                    edAct.chooseImageFrag?.updateAdapter(returnValues)

                }
            }
        } else if (resultCode == AppCompatActivity.RESULT_OK && requestCode == REQUEST_CODE_GET_SINGLE_IMAGE) {
            if (data != null) {
                val uris = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                edAct.chooseImageFrag?.setSingleImage(uris?.get(0)!!, edAct.editImagePos)
            }
        }

    }

}