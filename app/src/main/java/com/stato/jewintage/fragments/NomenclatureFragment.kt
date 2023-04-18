package com.stato.jewintage.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.stato.jewintage.MainActivity
import com.stato.jewintage.R
import com.stato.jewintage.adapters.AddRcAdapter
import com.stato.jewintage.databinding.FragmentNomenclatureBinding
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.model.AddSales
import com.stato.jewintage.model.DbManager
import com.stato.jewintage.viewmodel.FirebaseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NomenclatureFragment : Fragment(), AddRcAdapter.SellButtonClickListener {
    private var _binding: FragmentNomenclatureBinding? = null
    private var auth = Firebase.auth
    private val binding get() = _binding!!
    private lateinit var adapter: AddRcAdapter
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNomenclatureBinding.inflate(inflater, container, false)
        val view = binding.root
        swipeRefreshLayout = binding.nomenclatureFrag

        swipeRefreshLayout.setOnRefreshListener {
            initViewModel()
            firebaseViewModel.loadAllAds()
            swipeRefreshLayout.isRefreshing = false
        }

        binding.nomenclatureFrag.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        setupRecyclerView()

        // Инициализация ViewModel и обновление адаптера
        initViewModel()
        firebaseViewModel.loadAllAds()
        return view
    }

    private fun setupRecyclerView() {
        adapter = AddRcAdapter(requireActivity() as MainActivity, this)
        binding.rcViewNomenclature.layoutManager = LinearLayoutManager(requireContext())
        binding.rcViewNomenclature.adapter = adapter
    }

    private fun initViewModel() {
        firebaseViewModel.liveAdsData.observe(viewLifecycleOwner) {
            adapter.updateAdapter(it)

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    private fun createNewSale(
//        addNom: AddNom,
//        quantity: String,
//        sellPrice: String,
//        sellDate: String,
//        paymentMethod: String
//    ) {
//        val firebaseUser = auth.currentUser
//        if (firebaseUser != null) {
//            val uid = firebaseUser.uid
//            val sale = AddSales(
//                category = addNom.category,
//                description = addNom.description,
//                price = sellPrice,
//                date = sellDate,
//                mainImage = addNom.mainImage,
//                image2 = addNom.image2,
//                image3 = addNom.image3,
//                soldQuantity = quantity,
//                paymentMethod = paymentMethod,
//                id = addNom.id,
//                uid = uid
//            )
//
//            val dbManager = DbManager()
//            dbManager.saveSale(sale, object : DbManager.FinishWorkListener {
//                override fun onFinish(isDone: Boolean) {
//                    if (isDone) {
//                        // Обновление количества товара после продажи
//                        val newQuantity = (addNom.quantity!!.toInt() - quantity.toInt()).toString()
//                        dbManager.updateQuantity(
//                            addNom,
//                            newQuantity,
//                            object : DbManager.FinishWorkListener {
//                                override fun onFinish(isDone: Boolean) {
//                                    if (isDone) {
//                                        Toast.makeText(
//                                            requireActivity() as MainActivity,
//                                            "Данные продажи успешно сохранены",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    } else {
//                                        Toast.makeText(
//                                            requireActivity() as MainActivity,
//                                            "Ошибка при обновлении количества товара",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//                                }
//                            })
//                    } else {
//                        Toast.makeText(
//                            requireActivity() as MainActivity,
//                            "Ошибка при сохранении данных продажи",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            })
//        }
//    }



    private fun saveSaleData(
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

            dbManager.findSaleByDate(uid, addNom.id!!, sellDate, object : DbManager.FindSaleListener {
                override fun onFinish(saleKey: String?, sale: AddSales?) {
                    if (sale == null || saleKey == null) {
                        val newSale = AddSales(
                            category = addNom.category,
                            description = addNom.description,
                            price = sellPrice,
                            date = sellDate,
                            mainImage = addNom.mainImage,
                            image2 = addNom.image2,
                            image3 = addNom.image3,
                            soldQuantity = quantity,
                            paymentMethod = paymentMethod,
                            id = addNom.id,
                            uid = uid
                        )

                        dbManager.saveSale(newSale, object : DbManager.FinishWorkListener {
                            override fun onFinish(isDone: Boolean) {
                                if (isDone) {
                                    Toast.makeText(
                                        requireActivity() as MainActivity,
                                        "Данные продажи успешно сохранены",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        requireActivity() as MainActivity,
                                        "Ошибка при сохранении данных продажи",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        })
                    } else {
                        val newQuantity = (sale.soldQuantity!!.toInt() + quantity.toInt()).toString()
                        val newPrice = (sale.price!!.toInt() + sellPrice.toInt()).toString()

                        dbManager.updateSaleQuantityAndPrice(saleKey, newQuantity, newPrice, object : DbManager.FinishWorkListener {
                            override fun onFinish(isDone: Boolean) {
                                if (isDone) {
                                    Toast.makeText(
                                        requireActivity() as MainActivity,
                                        "Данные продажи успешно обновлены",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        requireActivity() as MainActivity,
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
                                requireActivity() as MainActivity,
                                "Количество товара успешно обновлено",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireActivity() as MainActivity,
                                "Ошибка при обновлении количества товара",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        }
    }






    private fun showSellDialog(addNom: AddNom) {
        val builder = AlertDialog.Builder(requireActivity() as MainActivity)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_sell, null)

        val etQuantity = dialogView.findViewById<TextInputEditText>(R.id.etQuantity)
        val etSellPrice = dialogView.findViewById<TextInputEditText>(R.id.etSellPrice)
        val tvSellDate = dialogView.findViewById<AutoCompleteTextView>(R.id.tvSellDate)
        val rgPaymentMethod = dialogView.findViewById<RadioGroup>(R.id.rgPaymentMethod)
        val btnSubmitSell = dialogView.findViewById<Button>(R.id.btnSubmitSell)
        // Получение оставшегося количества товара
//        var remainingQuantity = addNom.quantity!!.toInt()
//Значения по умолчанию в диалоге
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
                requireActivity() as MainActivity,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Обработка выбранной даты
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)
                    // Форматирование даты в строку
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val selectedDateStr = dateFormat.format(selectedDate.time)

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
                        requireActivity() as MainActivity,
                        "Введенное количество превышает текущее количество",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    saveSaleData(addNom, quantityInput, sellPrice, sellDate, paymentMethod)
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(
                    requireActivity() as MainActivity,
                    "Заполните все поля",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }



        override fun onSellButtonClick(addNom: AddNom) {
            showSellDialog(addNom)
        }

    }


