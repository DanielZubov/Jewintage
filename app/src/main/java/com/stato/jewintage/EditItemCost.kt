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
import com.stato.jewintage.databinding.ActivityEditCostItemBinding
import com.stato.jewintage.model.AddCost
import com.stato.jewintage.model.Category
import com.stato.jewintage.model.DbManager
import com.stato.jewintage.util.ImagePickerManager
import com.stato.jewintage.util.PermissionsManager
import com.stato.jewintage.viewmodel.FirebaseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class EditItemCost : AppCompatActivity() {
    lateinit var binding: ActivityEditCostItemBinding
    private val imageAdapter = ImageAdapter(this, null)
    private val dbManager = DbManager()
    private var isEditState = false
    private var cost: AddCost? = null
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
        binding = ActivityEditCostItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()
        binding.vpImages.adapter = imageAdapter
    }

    private fun checkEditState() {
        isEditState = isEditState()
        if (isEditState) {
            @Suppress("DEPRECATION")
            cost = intent.getSerializableExtra(MainActivity.ADS_DATA) as AddCost
            if (cost != null) fillViews(cost!!)
        }
    }

    private fun isEditState(): Boolean {
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fillViews(cost: AddCost) = with(binding) {
        edTICostCategory.setText(cost.category)
        edTIDescription.setText(cost.description)
        edTIPrice.setText(cost.price)
        edTICostDate.setText(cost.date)
        edTIquantity.setText(cost.quantity)
        imageAdapter.imageUri = Uri.parse(cost.mainImage)
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
            binding.edTICostCategory.setAdapter(arrayAdapter)
        }

        binding.ibAddCategory.setOnClickListener {
            val layout = LayoutInflater.from(this).inflate(R.layout.dialog_cost_category, null)
            val categoryNameEditText: TextInputEditText = layout.findViewById(R.id.etNameCategory)
            val categoryNameLayout: TextInputLayout = layout.findViewById(R.id.layoutNameCategory)
            val submitButton: Button = layout.findViewById(R.id.btnSubmitSave)

            val dialog = AlertDialog.Builder(this)
                .setView(layout)
                .setCancelable(true)
                .create()

            submitButton.setOnClickListener {
                var isFormValid = true
                val categoryName = categoryNameEditText.text.toString()

                if (categoryName.isBlank()) {
                    categoryNameLayout.error = getString(R.string.error_field_required)
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
                }
                dbManager.addNewCategory(
                    newCategory,
                    object : DbManager.FinishWorkListener {
                        override fun onFinish(isDone: Boolean) {
                            if (isDone) {
                                firebaseViewModel.loadAllCategories()
                                dialog.dismiss()
                            } else {
                                Toast.makeText(
                                    this@EditItemCost,
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
        binding.edTICostDate.setText(currentDateStr)
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
    fun onClickGetImagesCost(view: View) {
        val permissionsGranted = permissionsManager.checkPermissions()
        if (permissionsGranted) {
            imagePickerManager.chooseImage()
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        if (binding.edTICostCategory.text?.isEmpty() == true) {
            binding.layoutTICostCategory.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            binding.layoutTICostCategory.error = null
        }

        if (binding.edTIPrice.text?.isEmpty() == true) {
            binding.layoutTIPrice.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            binding.layoutTIPrice.error = null
        }

        if (binding.edTICostDate.text?.isEmpty() == true) {
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


    fun onClickPublishCost(view: View) {
        if (validateFields()) {
            binding.progressLayout.visibility = View.VISIBLE
            cost = fillAddCost()
            uploadImage()
        }
    }

    private fun onPublishFinish(): DbManager.FinishWorkListener {
        return object : DbManager.FinishWorkListener {
            override fun onFinish(isDone: Boolean) {
                binding.progressLayout.visibility = View.GONE
                if (isDone) finish()
            }
        }
    }

    private fun fillAddCost(): AddCost {
        val adTemp: AddCost
        binding.apply {
            val price = edTIPrice.text.toString().toFloat()
            val quantity = edTIquantity.text.toString().toFloat()
            val sum = price * quantity
            adTemp = AddCost(
                edTICostCategory.text.toString(),
                edTIDescription.text.toString(),
                edTIPrice.text.toString(),
                sum.toString(),
                edTICostDate.text.toString(),
                cost?.mainImage ?: "empty",
                edTIquantity.text.toString(),
                cost?.id ?: dbManager.dbCosts.push().key,
                dbManager.auth.uid
            )
        }
        return adTemp
    }

    private fun uploadImage() {
        if (selectedImageUri != null) {
            val byteArray = imagePickerManager.uriToCompressedByteArray(this, selectedImageUri!!, 200 * 1024)

            if (cost?.mainImage?.startsWith("http") == true) {
                updateImage(byteArray, cost?.mainImage!!) {
                    setImageUriToAddNom(it.result.toString())
                }
            } else {
                uploadImageDb(byteArray) {
                    setImageUriToAddNom(it.result.toString())
                }
            }
        } else {
            dbManager.publishCost(cost!!, onPublishFinish())
        }
    }

    private fun setImageUriToAddNom(uri: String) {
        cost = cost?.copy(mainImage = uri)
        dbManager.publishCost(cost!!, onPublishFinish())
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