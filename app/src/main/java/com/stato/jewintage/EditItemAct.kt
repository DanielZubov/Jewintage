package com.stato.jewintage

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.stato.jewintage.adapters.ImageAdapter
import com.stato.jewintage.databinding.ActivityEditItemBinding
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.model.Category
import com.stato.jewintage.model.DbManager
import com.stato.jewintage.util.ImagePickerManager
import com.stato.jewintage.util.PermissionsManager
import com.stato.jewintage.viewmodel.FirebaseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class EditItemAct : AppCompatActivity() {
    lateinit var binding: ActivityEditItemBinding
    private val imageAdapter = ImageAdapter(this, null)
    private val dbManager = DbManager()
    private var isEditState = false
    private var addNom: AddNom? = null
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private val permissionsManager = PermissionsManager(this)
    private var selectedImageUri: Uri? = null
    @SuppressLint("NotifyDataSetChanged")
    private val imagePickerManager = ImagePickerManager(this).apply {
        onImagePicked = { uri ->
            imageAdapter.imageUri = uri
            imageAdapter.notifyDataSetChanged()
            selectedImageUri = uri
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()
        binding.vpImages.adapter = imageAdapter
    }

    private fun checkEditState() {
        isEditState = isEditState()
        if (isEditState) {
            @Suppress("DEPRECATION")
            addNom = intent.getSerializableExtra(MainActivity.ADS_DATA) as AddNom
            if (addNom != null) fillViews(addNom!!)
        }
    }

    private fun isEditState(): Boolean {
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fillViews(addNom: AddNom) = with(binding) {
        edTICategory.setText(addNom.category)
        edTIDescription.setText(addNom.description)
        edTIPrice.setText(addNom.price)
        edTIDate.setText(addNom.date)
        edTIquantity.setText(addNom.quantity)
        imageAdapter.imageUri = Uri.parse(addNom.mainImage)
        imageAdapter.notifyDataSetChanged()
    }

    private fun init() {
        firebaseViewModel.loadAllCategories()
    }

    override fun onResume() {
        super.onResume()
        firebaseViewModel.loadAllCategories()
        firebaseViewModel.liveCategoryData.observe(this) { categoryList ->
            val categories = categoryList.map { it.name }
            val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_category, categories)
            binding.edTICategory.setAdapter(arrayAdapter)
        }

        binding.ibAddCategory.setOnClickListener {
            val layout = LayoutInflater.from(this).inflate(R.layout.dialog_new_category, null)

            val categoryNameEditText: TextInputEditText = layout.findViewById(R.id.etNameCategory)
            val layoutCategory: TextInputLayout = layout.findViewById(R.id.layoutNewCategory)
            val layoutCategoryCommission: TextInputLayout = layout.findViewById(R.id.layoutNewCategoryCommission)
            val categoryCommissionEditText: TextInputEditText = layout.findViewById(R.id.etCommissionCategory)
            val submitButton: Button = layout.findViewById(R.id.btnSubmitSave)

            val dialog = AlertDialog.Builder(this)
                .setView(layout)
                .setCancelable(true)
                .create()

            submitButton.setOnClickListener {
                var isFormValid = true
                val categoryName = categoryNameEditText.text.toString()
                val categoryCommission = categoryCommissionEditText.text.toString().toFloatOrNull()

                if (categoryName.isBlank()) {
                    layoutCategory.error = getString(R.string.error_field_required)
                    isFormValid = false
                }

                if (categoryCommission == null) {
                    layoutCategoryCommission.error = getString(R.string.error_field_required)
                    categoryCommissionEditText.setText("0")
                    isFormValid = false
                }

                if (!isFormValid) {
                    @Suppress("LABEL_NAME_CLASH")
                    return@setOnClickListener
                }

                val newCategory = Category().apply {
                    id = dbManager.dbCategories.push().key
                    uid = dbManager.auth.uid
                    name = categoryName
                    commission = categoryCommission
                }
                dbManager.addNewCategory(
                    newCategory,
                    object : DbManager.FinishWorkListener {
                        override fun onFinish(isDone: Boolean) {
                            if (isDone) {
                                // обновляем список категорий
                                firebaseViewModel.loadAllCategories()
                                dialog.dismiss() // Закрываем диалог только при успешном сохранении
                            } else {
                                Toast.makeText(
                                    this@EditItemAct,
                                    getString(R.string.error_saving_in_the_database),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    })
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }



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
                    val formatDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val selectedDateStr = formatDate.format(selectedDate.time)

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
        binding.edTIDescription.doOnTextChanged { text, _, _, _ ->
            if (text!!.length > 50) {
                binding.layoutTIDescription.error = getString(R.string.warning_description)
            } else if (text.length < 50) {
                binding.layoutTIDescription.error = null
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionsManager.PERMISSIONS_REQUEST_CODE) {
            val indexOfCameraPermission = permissions.indexOf(Manifest.permission.CAMERA)

            if (indexOfCameraPermission >= 0 && grantResults[indexOfCameraPermission] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение на камеру было предоставлено
                imagePickerManager.chooseImage()
            } else {
                // Разрешение на камеру было отклонено
                showDialogPermissionNeeded()
            }
            return
        }
    }


    private fun showDialogPermissionNeeded() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.permissions_required))
        builder.setMessage(getString(R.string.permissions_discription))
        builder.setPositiveButton(getString(R.string.permissions_grant)) { _, _ ->
            permissionsManager.checkPermissions()
        }
        builder.setNegativeButton(getString(R.string.permissions_cancel)) { _, _ ->
            // Действия, которые должны быть выполнены, если пользователь отказывается предоставить разрешения
        }
        val dialog = builder.create()
        dialog.show()
    }


    fun onClickGetImages(view: View) {
        val permissionsGranted = permissionsManager.checkPermissions()
        if (permissionsGranted) {
            imagePickerManager.chooseImage()
        }
    }


    fun onClickPublishNum(view: View) {
        if (validateFields()) {
            binding.progressLayout.visibility = View.VISIBLE
            addNom = fillAddNum()
            uploadImage()
        }
    }
    private fun fillAddNum(): AddNom {
        val adTemp: AddNom
        binding.apply {
            val price = edTIPrice.text.toString().toFloat()
            val quantity = edTIquantity.text.toString().toFloat()
            val sum = price * quantity
            adTemp = AddNom(
                edTICategory.text.toString(),
                edTIDescription.text.toString(),
                edTIPrice.text.toString(),
                sum.toString(),
                edTIDate.text.toString(),
                edTIquantity.text.toString(),
                addNom?.mainImage ?: "empty",
                addNom?.id ?: dbManager.db.push().key,
                dbManager.auth.uid
            )
        }
        return adTemp
    }


    private fun validateFields(): Boolean {
        var isValid = true

        if (binding.edTICategory.text?.isEmpty() == true) {
            binding.layoutTINumCategory.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            binding.layoutTINumCategory.error = null
        }

        if (binding.edTIPrice.text?.isEmpty() == true) {
            binding.layoutTIPrice.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            binding.layoutTIPrice.error = null
        }

        if (binding.edTIDate.text?.isEmpty() == true) {
            binding.layoutTIDate.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            binding.layoutTIDate.error = null
        }

        if (binding.edTIquantity.text?.isEmpty() == true) {
            binding.layoutTIquantity.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            binding.layoutTIquantity.error = null
        }

        return isValid
    }


    private fun onPublishFinish(): DbManager.FinishWorkListener {
        return object : DbManager.FinishWorkListener {
            override fun onFinish(isDone: Boolean) {
                binding.progressLayout.visibility = View.GONE
                if (isDone) finish()
            }
        }
    }


    private fun uploadImage() {
        if (selectedImageUri != null) {
            val byteArray = imagePickerManager.uriToCompressedByteArray(this, selectedImageUri!!, 200 * 1024)
            if (addNom?.mainImage?.startsWith("http") == true) {
                updateImage(byteArray, addNom?.mainImage!!) {
                    setImageUriToAddNom(it.result.toString())
                }
            } else {
                uploadImageDb(byteArray) {
                    setImageUriToAddNom(it.result.toString())
                }
            }
        } else {
            dbManager.publishAdd(addNom!!, onPublishFinish())
        }
    }
    private fun setImageUriToAddNom(uri: String) {
        addNom = addNom?.copy(mainImage = uri)
        dbManager.publishAdd(addNom!!, onPublishFinish())
    }

    private fun uploadImageDb(byteArray: ByteArray, listener: OnCompleteListener<Uri>) {
        val imStorageReference = dbManager.dbStorage.child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")
        val upTask = imStorageReference.putBytes(byteArray)
        upTask.continueWithTask {
            imStorageReference.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun updateImage(byteArray: ByteArray, url: String, listener: OnCompleteListener<Uri>) {
        val imStorageReference = dbManager.dbStorage.storage
            .getReferenceFromUrl(url)
        val upTask = imStorageReference.putBytes(byteArray)
        upTask.continueWithTask {
            imStorageReference.downloadUrl
        }.addOnCompleteListener(listener)
    }
}