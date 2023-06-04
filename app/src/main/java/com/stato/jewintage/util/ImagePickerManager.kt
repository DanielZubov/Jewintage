package com.stato.jewintage.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.MediaStore
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.stato.jewintage.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImagePickerManager(private val activity: AppCompatActivity) {
    var onImagePicked: ((uri: Uri) -> Unit)? = null


    private lateinit var currentPhotoPath: String

    private val pickImageFromGallery = activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            handleImageUri(uri)
        }
    }

    private val takePhoto = activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess: Boolean ->
        if (isSuccess) {
            val fileUri = Uri.parse(currentPhotoPath)
            handleImageUri(fileUri)
        }
    }

    @Suppress("DEPRECATION")
    fun uriToCompressedByteArray(context: Context, uri: Uri, maxBytes: Int): ByteArray {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        val byteStream = ByteArrayOutputStream()

        var quality = 100
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteStream)

        while (byteStream.size() > maxBytes && quality > 0) {
            byteStream.reset()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteStream)
        }

        return byteStream.toByteArray()
    }

    fun chooseImage() {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_choose_photo, null)
        val btnTakePhoto = dialogLayout.findViewById<Button>(R.id.btnTakePhoto)
        val btnChooseGallery = dialogLayout.findViewById<Button>(R.id.btnChooseGallery)
        builder.setView(dialogLayout)
        val dialog = builder.create()
        
        btnTakePhoto.setOnClickListener {
            takePhoto()
            dialog.dismiss()
        }

        btnChooseGallery.setOnClickListener {
            chooseFromGallery()
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }


    private fun takePhoto() {
        try {
            val photoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(
                activity,
                "com.stato.jewintage.fileprovider",
                photoFile
            )
            takePhoto.launch(photoURI)
        } catch (ex: IOException) {
            // Handle error
        }
    }


    private fun chooseFromGallery() {
        pickImageFromGallery.launch("image/*")
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = activity.getExternalFilesDir(null)
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        currentPhotoPath = "file:${image.absolutePath}"
        return image
    }

    private fun handleImageUri(uri: Uri) {
        onImagePicked?.invoke(uri)
    }

}
