package com.stato.jewintage

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.tasks.OnCompleteListener
import com.stato.jewintage.adapters.ImageAdapter
import com.stato.jewintage.databinding.ActivityEditCostItemBinding
import com.stato.jewintage.fragments.FragmentCloseInterface
import com.stato.jewintage.fragments.ImageListFragment
import com.stato.jewintage.model.AddCost
import com.stato.jewintage.model.DbManager
import com.stato.jewintage.util.ImageManager
import com.stato.jewintage.util.ImagePicker
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class EditItemCost : AppCompatActivity(), FragmentCloseInterface {
    var chooseImageFrag: ImageListFragment? = null
    lateinit var binding: ActivityEditCostItemBinding
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()
    var editImagePos = 0
    private var imageIndex = 0
    private var isEditState = false
    private var cost: AddCost? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCostItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()

    }

    private fun checkEditState(){
        isEditState = isEditState()
        if (isEditState){
            @Suppress("DEPRECATION")
            cost = intent.getSerializableExtra(MainActivity.ADS_DATA) as AddCost
            if (cost != null)fillViews(cost!!)
        }
    }

    private fun isEditState(): Boolean{
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(cost: AddCost) = with(binding){
        edTICostCategory.setText(cost.category)
        edTIDescription.setText(cost.description)
        edTIPrice.setText(cost.price)
        edTICostDate.setText(cost.date)
        edTIquantity.setText(cost.quantity)
        ImageManager.fillImageCostArray(cost, imageAdapter)
    }

    private fun init() {
        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter
    }

    override fun onResume() {
        super.onResume()
        val category = resources.getStringArray(R.array.category_cost)
        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_category, category)
        binding.edTICostCategory.setAdapter(arrayAdapter)
        binding.edTICostDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)
                    val formatDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val selectedDateStr = formatDate.format(selectedDate.time)
                    binding.edTICostDate.setText(selectedDateStr)
                },
                year,
                month,
                day
            )
            val minDate = Calendar.getInstance()
            minDate.set(2023, 0, 1)
            datePickerDialog.datePicker.minDate = minDate.timeInMillis
            datePickerDialog.show()
        }
        binding.edTIDescription.doOnTextChanged { text, _, _, _ ->
            if (text!!.length > 50) {
                binding.layoutTIDescription.error = "Превышено максимальное количество символов"
            } else if (text.length < 50) {
                binding.layoutTIDescription.error = null
            }
        }
    }


    fun onClickGetImagesCost(view: View) {
        if (imageAdapter.mainArray.size == 0) {
            ImagePicker.getMultiImagesCost(this,3)
        } else {
            openChooseItemFrag(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }

    }
    private fun validateFields(): Boolean {
        var isValid = true

        if (binding.edTICostCategory.text?.isEmpty() == true) {
            binding.layoutTICostCategory.error = "Поле не может быть пустым"
            isValid = false
        } else {
            binding.layoutTICostCategory.error = null
        }

        if (binding.edTIPrice.text?.isEmpty() == true) {
            binding.layoutTIPrice.error = "Поле не может быть пустым"
            isValid = false
        } else {
            binding.layoutTIPrice.error = null
        }

        if (binding.edTICostDate.text?.isEmpty() == true) {
            binding.layoutTIDate.error = "Поле не может быть пустым"
            isValid = false
        } else {
            binding.layoutTIDate.error = null
        }

        if (binding.edTIquantity.text?.isEmpty() == true) {
            binding.layoutTIquantity.error = "Поле не может быть пустым"
            isValid = false
        } else {
            binding.layoutTIquantity.error = null
        }

        return isValid
    }



    fun onClickPublishCost(view: View) {
        if (validateFields()) {
        binding.progressLayout.visibility = View.VISIBLE
        cost = fillAddCost()
        uploadImages()
        }
    }

    private fun onPublishFinish(): DbManager.FinishWorkListener{
        return object: DbManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                binding.progressLayout.visibility = View.GONE
                if(isDone) {
                    finish()
                } else {
                    Toast.makeText(
                        this@EditItemCost,
                        "Ошибка",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

    }

    private fun fillAddCost(): AddCost {
        val adTemp: AddCost
        binding.apply {
            adTemp = AddCost(
                edTICostCategory.text.toString(),
                edTIDescription.text.toString(),
                edTIPrice.text.toString(),
                edTICostDate.text.toString(),
                cost?.mainImage ?:"empty",
                cost?.image2 ?:"empty",
                cost?.image3 ?:"empty",
                edTIquantity.text.toString(),
                cost?.id ?: dbManager.dbCosts.push().key,
                dbManager.auth.uid

            )
        }
        return adTemp
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrollViewMain.visibility = View.VISIBLE
        binding.scrollbtnLayout.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
    }

    fun openChooseItemFrag(newList: ArrayList<Uri>?) {

        chooseImageFrag = ImageListFragment(this)
        if (newList != null)chooseImageFrag?.resizeSelectedImages(newList, true, this)
        binding.scrollViewMain.visibility = View.GONE
        binding.scrollbtnLayout.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.placeHolder, chooseImageFrag!!)
        fm.commit()

    }

    private fun uploadImages() {
        if (imageIndex == 3) {
            dbManager.publishCost(cost!!, onPublishFinish())
            return
        }
        val oldUrl = getUrlFromAd()
        if (imageAdapter.mainArray.size > imageIndex) {

            val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex])
            if (oldUrl.startsWith("http")) {
                updateImage(byteArray, oldUrl) {
                    nextImage(it.result.toString())
                }
            } else {
                uploadImage(byteArray) {
                    nextImage(it.result.toString())
                }
            }

        } else {
            if (oldUrl.startsWith("http")) {
                deleteImageByUrl(oldUrl) {
                    nextImage("empty")
                }
            } else {
                nextImage("empty")
            }
        }
    }


    private fun nextImage(uri: String){
        setImageUriToAddNom(uri)
        imageIndex++
        uploadImages()
    }

    private fun setImageUriToAddNom(uri: String){
        when(imageIndex){
            0 -> cost = cost?.copy(mainImage = uri)
            1 -> cost = cost?.copy(image2 = uri)
            2 -> cost = cost?.copy(image3 = uri)
        }
    }

    private fun getUrlFromAd(): String{
        return listOf(
            cost?.mainImage!!,
            cost?.image2!!,
            cost?.image3!!
        )[imageIndex]
    }
    private fun prepareImageByteArray(bitmap: Bitmap): ByteArray{
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 20, outStream)
        return outStream.toByteArray()
    }
    private fun uploadImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>){
        val imStorageReference = dbManager.dbStorage.child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")
        val upTask = imStorageReference.putBytes(byteArray)
        upTask.continueWithTask{
            imStorageReference.downloadUrl
        }.addOnCompleteListener(listener)
    }
    private fun updateImage(byteArray: ByteArray, url: String, listener: OnCompleteListener<Uri>){
        val imStorageReference = dbManager.dbStorage.storage
            .getReferenceFromUrl(url)
        val upTask = imStorageReference.putBytes(byteArray)
        upTask.continueWithTask{
            imStorageReference.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun deleteImageByUrl(oldUrl: String, listener: OnCompleteListener<Void>){
        dbManager.dbStorage.storage
            .getReferenceFromUrl(oldUrl)
            .delete().addOnCompleteListener(listener)
    }


}