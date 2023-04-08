package com.stato.jewintage

import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.fxn.utility.PermUtil
import com.stato.jewintage.adapters.ImageAdapter
import com.stato.jewintage.data.AddNom
import com.stato.jewintage.databinding.ActivityEditItemBinding
import com.stato.jewintage.db.DbManager
import com.stato.jewintage.fragments.FragmentCloseInterface
import com.stato.jewintage.fragments.ImageListFragment
import com.stato.jewintage.util.ImagePicker
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

    }

    private fun init() {

        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImagePicker.showSelectedImages(resultCode, requestCode, data, this)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.getImages(this, 3, ImagePicker.REQUEST_CODE_GET_IMAGES)
                } else {

                    Toast.makeText(
                        this,
                        "Approve permissions to open Pix ImagePicker",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
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
            ImagePicker.getImages(this, 3, ImagePicker.REQUEST_CODE_GET_IMAGES)
        } else {
            openChooseItemFrag(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }

    }

    fun onClickPublishNum(view: View){
        dbManager.publishAdd(fillAddNum())
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
                dbManager.db.push().key

            )
        }
        return addNom
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
    }

    fun openChooseItemFrag(newList: ArrayList<String>?) {

        chooseImageFrag = ImageListFragment(this, newList)
        binding.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.placeHolder, chooseImageFrag!!)
        fm.commit()

    }

}