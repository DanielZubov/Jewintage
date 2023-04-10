package com.stato.jewintage

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.tasks.OnCompleteListener
import com.stato.jewintage.adapters.ImageAdapter
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.databinding.ActivityEditItemBinding
import com.stato.jewintage.model.DbManager
import com.stato.jewintage.fragments.FragmentCloseInterface
import com.stato.jewintage.fragments.ImageListFragment
import com.stato.jewintage.util.ImagePicker
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class EditItemAct : AppCompatActivity(), FragmentCloseInterface {
    var chooseImageFrag: ImageListFragment? = null
    lateinit var binding: ActivityEditItemBinding
    private var isImagePermissionGranted = false
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()
    var editImagePos = 0
    private var imageIndex = 0
    private var isEditState = false
    private var addNom: AddNom? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()

    }

    private fun checkEditState(){
        isEditState = isEditState()
        if (isEditState){
            addNom = intent.getSerializableExtra(MainActivity.ADS_DATA) as AddNom
            if (addNom != null)fillViews(addNom!!)
        }
    }

    private fun isEditState(): Boolean{
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(addNom: AddNom) = with(binding){
        edTICategory.setText(addNom.category)
        edTIDescription.setText(addNom.description)
        edTIPrice.setText(addNom.price)
        edTIDate.setText(addNom.date)
        edTIquantity.setText(addNom.quantity)
    }

    private fun init() {

        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter

    }

    override fun onResume() {
        super.onResume()
        val category = resources.getStringArray(R.array.category)
        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_category, category)
        binding.edTICategory.setAdapter(arrayAdapter)
        // Получение текущей даты
        val currentDate = Calendar.getInstance().time

// Форматирование даты в строку
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentDateStr = dateFormat.format(currentDate)

// Установка даты в поле ввода
        binding.edTIDate.setText(currentDateStr)

        // Слушатель клика на поле ввода даты
        binding.edTIDate.setOnClickListener {
            // Получение текущей даты
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Создание диалога выбора даты
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Обработка выбранной даты
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)
                    // Форматирование даты в строку
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val selectedDateStr = dateFormat.format(selectedDate.time)

                    // Установка выбранной даты в поле ввода
                    binding.edTIDate.setText(selectedDateStr)
                },
                year,
                month,
                day
            )

            // Установка диапазона дат
            val minDate = Calendar.getInstance()
            minDate.set(2023, 0, 1)
            datePickerDialog.datePicker.minDate = minDate.timeInMillis
//            datePickerDialog.datePicker.maxDate = calendar.timeInMillis

            // Показ диалога выбора даты
            datePickerDialog.show()
        }
        //Прослушивание ошибок в поле ввода "Описание"
        binding.edTIDescription.doOnTextChanged { text, start, before, count ->
            if (text!!.length > 50) {
                binding.layoutTINumCategory.error = "Превышено максимальное количество символов"
            } else if (text.length < 50) {
                binding.layoutTINumCategory.error = null
            }
        }

    }

    fun onClickGetImages(view: View) {
        binding.vpImages.visibility = View.VISIBLE
        if (imageAdapter.mainArray.size == 0) {
            ImagePicker.getMultiImages(this,3)
        } else {
            openChooseItemFrag(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }

    }

    fun onClickPublishNum(view: View){
        addNom = fillAddNum()
        if (isEditState) {
            addNom?.copy(id = addNom?.id)?.let { dbManager.publishAdd(it, onPublishFinish()) }
        } else{
            uploadImages()
        }
    }

    private fun onPublishFinish(): DbManager.FinishWorkListener{
        return object: DbManager.FinishWorkListener{
            override fun onFinish() {
                finish()
            }

        }

    }

    private fun fillAddNum(): AddNom {
        val addNom: AddNom
        binding.apply {
            addNom = AddNom(
                edTICategory.text.toString(),
                edTIDescription.text.toString(),
                edTIPrice.text.toString(),
                edTIDate.text.toString(),
                edTIquantity.text.toString(),
                "empty",
                "empty",
                "empty",
                dbManager.db.push().key,
                dbManager.auth.uid

            )
        }
        return addNom
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
    }

    fun openChooseItemFrag(newList: ArrayList<Uri>?) {

        chooseImageFrag = ImageListFragment(this)
        if (newList != null)chooseImageFrag?.resizeSelectedImages(newList, true, this)
        binding.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.placeHolder, chooseImageFrag!!)
        fm.commit()

    }

    private fun uploadImages(){
        if (imageAdapter.mainArray.size == imageIndex){
            dbManager.publishAdd(addNom!!, onPublishFinish())
            return
        }
        val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex])
        uploadImage(byteArray){
//            dbManager.publishAdd(addNom!!, onPublishFinish())
            nextImage(it.result.toString())
        }
    }
    private fun nextImage(uri: String){
        setImageUriToAddNom(uri)
        imageIndex++
        uploadImages()
    }

    private fun setImageUriToAddNom(uri: String){
        when(imageIndex){
            0 -> addNom = addNom?.copy(mainImage = uri)
            1 -> addNom = addNom?.copy(image2 = uri)
            2 -> addNom = addNom?.copy(image3 = uri)
        }
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
            task->imStorageReference.downloadUrl
        }.addOnCompleteListener(listener)
    }

}