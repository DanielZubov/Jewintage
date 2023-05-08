package com.stato.jewintage

import android.app.DatePickerDialog
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.model.AddSales
import com.stato.jewintage.model.DbManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun AppCompatActivity.showSellDialog(auth: FirebaseAuth, addNom: AddNom) {
    val builder = AlertDialog.Builder(this)
    val inflater = layoutInflater
    val dialogView = inflater.inflate(R.layout.dialog_sell, null)

    val etQuantity = dialogView.findViewById<TextInputEditText>(R.id.etQuantity)
    val etSellPrice = dialogView.findViewById<TextInputEditText>(R.id.etSellPrice)
    val tvSellDate = dialogView.findViewById<AutoCompleteTextView>(R.id.tvSellDate)
    val rgPaymentMethod = dialogView.findViewById<RadioGroup>(R.id.rgPaymentMethod)
    val rbCash = dialogView.findViewById<RadioButton>(R.id.rbCash)
    val btnSubmitSell = dialogView.findViewById<Button>(R.id.btnSubmitSell)
    // Получение оставшегося количества товара
//        var remainingQuantity = addNom.quantity!!.toInt()
//Значения по умолчанию в диалоге

    rbCash.isChecked = true
    etSellPrice.setText(addNom.price)
    etQuantity.setText("1")

    builder.setView(dialogView)
    val dialog = builder.create()
    dialog.show()

    // Получение текущей даты
    val currentDate = Calendar.getInstance().time

// Форматирование даты в строку
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val currentDateStr = dateFormat.format(currentDate)

// Установка даты в поле ввода
    tvSellDate.setText(currentDateStr)

    // Слушатель клика на поле ввода даты
    tvSellDate.setOnClickListener {
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
                tvSellDate.setText(selectedDateStr)
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

    btnSubmitSell.setOnClickListener {
        val quantityInput = etQuantity.text.toString()
        val sellPrice = etSellPrice.text.toString()
        val sellDate = tvSellDate.text.toString()
        val selectedPaymentMethodId = rgPaymentMethod.checkedRadioButtonId
        val radioButton = dialogView.findViewById<RadioButton>(selectedPaymentMethodId)
        val paymentMethod = radioButton.text.toString()

        if (quantityInput.isNotBlank() && sellPrice.isNotBlank() && selectedPaymentMethodId != -1) {
            // Проверка, чтобы введенное количество не превышало текущее количество товара
            val currentQuantity = addNom.quantity!!.toInt()
            val inputQuantity = quantityInput.toInt()

            if (inputQuantity > currentQuantity) {
                Toast.makeText(
                    this,
                    "Введенное количество превышает текущее количество",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                saveSaleData(auth, addNom, quantityInput, sellPrice, sellDate, paymentMethod)
                dialog.dismiss()
            }
        } else {
            Toast.makeText(
                this,
                "Заполните все поля",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

fun AppCompatActivity.saveSaleData(
    auth: FirebaseAuth,
    addNom: AddNom,
    quantity: String,
    sellPrice: String,
    sellDate: String,
    paymentMethod: String
) {
    val firebaseUser = auth.currentUser
    if (firebaseUser != null) {
        val uid = firebaseUser.uid
        val dbManager = DbManager()

        dbManager.findSaleByDate(
            uid,
            addNom.id!!,
            sellDate,
            paymentMethod,
            object : DbManager.FindSaleListener {
                override fun onFinish(saleKey: String?, sale: AddSales?) {
                    val upPrice = (sellPrice.toInt() * quantity.toInt()).toString()
                    if (sale == null || saleKey == null) {
                        val newSaleKey = dbManager.dbSales.push().key
                        val newSale = AddSales(
                            category = addNom.category,
                            description = addNom.description,
                            price = upPrice,
                            date = sellDate,
                            mainImage = addNom.mainImage,
                            image2 = addNom.image2,
                            image3 = addNom.image3,
                            soldQuantity = quantity,
                            paymentMethod = paymentMethod,
                            id = addNom.id,
                            uid = uid,
                            idItem = newSaleKey
                        )

                        dbManager.saveSale(newSale, object : DbManager.FinishWorkListener {
                            override fun onFinish(isDone: Boolean) {
                                if (isDone) {
                                    Toast.makeText(
                                        this@saveSaleData,
                                        "Данные продажи успешно сохранены",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@saveSaleData,
                                        "Ошибка при сохранении данных продажи",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        })
                    } else {
                        val newQuantity =
                            (sale.soldQuantity!!.toInt() + quantity.toInt()).toString()
                        val newPrice = (sale.price!!.toInt() + (sellPrice.toInt() * quantity.toInt())).toString()

                        dbManager.updateSaleQuantityAndPrice(
                            addNom.uid!!,
                            saleKey,
                            newQuantity,
                            newPrice,
                            object : DbManager.FinishWorkListener {
                                override fun onFinish(isDone: Boolean) {
                                    if (isDone) {
                                        Toast.makeText(
                                            this@saveSaleData,
                                            "Данные продажи успешно обновлены",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@saveSaleData,
                                            "Ошибка при обновлении данных продажи",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            })
                    }
                }
            })

        // Обновление количества товара после продажи
        val newQuantity = (addNom.quantity!!.toInt() - quantity.toInt()).toString()
        dbManager.updateQuantity(
            addNom,
            newQuantity,
            object : DbManager.FinishWorkListener {
                override fun onFinish(isDone: Boolean) {
                    if (isDone) {
                        Toast.makeText(
                            this@saveSaleData,
                            "Количество товара успешно обновлено",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@saveSaleData,
                            "Ошибка при обновлении количества товара",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }
}

