package com.stato.jewintage.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.Group
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.stato.jewintage.EditItemAct
import com.stato.jewintage.MainActivity
import com.stato.jewintage.R
import com.stato.jewintage.adapters.AddRcAdapter
import com.stato.jewintage.adapters.CategoryFilterAdapter
import com.stato.jewintage.databinding.FragmentNomenclatureBinding
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.model.AddSales
import com.stato.jewintage.model.DbManager
import com.stato.jewintage.viewmodel.FirebaseViewModel
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NomenclatureFragment : Fragment(), AddRcAdapter.SellButtonClickListener {
    private var _binding: FragmentNomenclatureBinding? = null
    private lateinit var drawerLayout: DrawerLayout
    private val binding get() = _binding!!
    private var auth = Firebase.auth
    private lateinit var adapter: AddRcAdapter
    private lateinit var categoryFilterAdapter: CategoryFilterAdapter
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
        _binding = FragmentNomenclatureBinding.inflate(inflater, container, false)

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
        initViewModel()
        firebaseViewModel.loadAllAds()
        drawerLayout = binding.drawerLayout
        categoryFilterAdapter = CategoryFilterAdapter(firebaseViewModel) { _, _ ->
            // Здесь вы можете обрабатывать выбор категории
        }
        val categoriesRecyclerView = binding.drawerFilter.categoriesRecyclerView
        categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        categoriesRecyclerView.adapter = categoryFilterAdapter

        binding.drawerFilter.dateFromEditText.setOnClickListener {
            showDatePickerDialog(binding.drawerFilter.dateFromEditText)
        }

        binding.drawerFilter.dateToEditText.setOnClickListener {
            showDatePickerDialog(binding.drawerFilter.dateToEditText)
        }
        binding.drawerFilter.applyFiltersButton.setOnClickListener {
            applyFilters()
        }
        binding.drawerFilter.resetButton.setOnClickListener {
            binding.drawerFilter.minPriceEditText.setText("")
            binding.drawerFilter.maxPriceEditText.setText("")
            binding.drawerFilter.dateFromEditText.setText("")
            binding.drawerFilter.dateToEditText.setText("")
            categoryFilterAdapter.resetCheckedCategories()
        }


        setupGroupsVisibility()
        setupFilterItemClickListeners()

        return binding.root
    }

    private fun applyFilters() {
        // Закрываем фильтр-меню
        drawerLayout.closeDrawer(GravityCompat.END)

        // Получаем значения фильтров
        val selectedCategories = categoryFilterAdapter.checkedCategories
        val minPrice = binding.drawerFilter.minPriceEditText.text.toString().toDoubleOrNull()
        val maxPrice = binding.drawerFilter.maxPriceEditText.text.toString().toDoubleOrNull()
        val dateFrom =
            binding.drawerFilter.dateFromEditText.text.toString().takeIf { it.isNotEmpty() }
        val dateTo = binding.drawerFilter.dateToEditText.text.toString().takeIf { it.isNotEmpty() }

        firebaseViewModel.filterAds(selectedCategories, minPrice, maxPrice, dateFrom, dateTo)
    }

    private fun showDatePickerDialog(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val formatDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val selectedDateStr = formatDate.format(selectedDate.time)
                editText.setText(selectedDateStr)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun setupGroupsVisibility() {
        binding.drawerFilter.categoriesGroup.visibility = View.GONE
        binding.drawerFilter.priceGroup.visibility = View.GONE
        binding.drawerFilter.dateGroup.visibility = View.GONE
    }

    private fun setupFilterItemClickListeners() {
        binding.drawerFilter.categoriesTitle.setOnClickListener {
            toggleGroupVisibility(binding.drawerFilter.categoriesGroup)
        }

        binding.drawerFilter.priceTitle.setOnClickListener {
            toggleGroupVisibility(binding.drawerFilter.priceGroup)
        }

        binding.drawerFilter.dateTitle.setOnClickListener {
            toggleGroupVisibility(binding.drawerFilter.dateGroup)
        }
    }

    private fun toggleGroupVisibility(group: Group) {
        group.visibility = if (group.visibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }


    private fun onClickAddNewNom() {
        val i = Intent(requireActivity() as MainActivity, EditItemAct::class.java)
        startActivity(i)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.options, menu)
        val searchItem = menu.findItem(R.id.actionSearch)
        val searchView = searchItem.actionView as SearchView
        searchView.isSubmitButtonEnabled = true

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    adapter.filter(newText)
                }
                return true
            }
        })

    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter_menu -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END)
                } else {
                    drawerLayout.openDrawer(GravityCompat.END)
                }
                return true
            }
        }
        @Suppress("DEPRECATION")
        return super.onOptionsItemSelected(item)
    }


    private fun setupRecyclerView() {
        adapter = AddRcAdapter(requireActivity() as MainActivity, this)
        binding.rcViewNomenclature.layoutManager = LinearLayoutManager(requireContext())
        binding.rcViewNomenclature.adapter = adapter
        binding.fbAddNom.setOnClickListener {
            onClickAddNewNom()
        }
    }

    private fun initViewModel() {
        firebaseViewModel.liveAdsData.observe(viewLifecycleOwner) { newData ->
            adapter.setData(newData)
        }
        firebaseViewModel.loadAllAds()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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

            // Запускаем новую корутину
            lifecycleScope.launch {
                // Получаем комиссии
                val categoryCommission =
                    dbManager.getCommissionCategory(uid, addNom.category!!) ?: 0f
                val paymentMethodCommission =
                    dbManager.getCommissionPaymentMethod(uid, paymentMethod) ?: 0f
                val priceMultiplier: Float
                if (paymentMethod == "Visa/MasterCard") {
                    priceMultiplier =
                        (1 - (categoryCommission / 100)) * (1 - (paymentMethodCommission / 100))
                    Log.d(
                        "MyLog",
                        "Visa/MasterCard: $priceMultiplier, $categoryCommission, $paymentMethodCommission"
                    )
                } else {
                    priceMultiplier = 1 - ((categoryCommission + paymentMethodCommission) / 100)
                    Log.d(
                        "MyLog",
                        "Cash: $priceMultiplier, $categoryCommission, $paymentMethodCommission")
                }

                dbManager.findSaleByDate(
                    uid,
                    addNom.id!!,
                    sellDate,
                    paymentMethod,
                    object : DbManager.FindSaleListener {
                        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
                            decimalSeparator = '.'
                        }
                        val df = DecimalFormat("#.##", symbols)
                        val newPrice =
                            df.format(sellPrice.toFloat() * quantity.toFloat() * priceMultiplier)


                        override fun onFinish(saleKey: String?, sale: AddSales?) {
                            if (sale == null || saleKey == null) {
                                val newSaleKey = dbManager.dbSales.push().key
                                val newSale = AddSales(
                                    category = addNom.category,
                                    description = addNom.description,
                                    price = newPrice,
                                    date = sellDate,
                                    mainImage = addNom.mainImage,
                                    image2 = addNom.image2,
                                    image3 = addNom.image3,
                                    soldQuantity = quantity,
                                    paymentMethod = paymentMethod,
                                    id = newSaleKey,
                                    uid = uid,
                                    idItem = addNom.id
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
                                val newQuantity =
                                    (sale.soldQuantity!!.toInt() + quantity.toInt()).toString()
                                val newPrice =
                                    df.format(sale.price!!.toFloat() + (newPrice.toFloat() * quantity.toFloat()))
                                        .toString()

                                dbManager.updateSaleQuantityAndPrice(
                                    addNom.uid!!,
                                    saleKey,
                                    newQuantity,
                                    newPrice,
                                    object : DbManager.FinishWorkListener {
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
    }

    private fun showSellDialog(addNom: AddNom) {
        val builder = AlertDialog.Builder(requireActivity() as MainActivity)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_sell, null)

        val etQuantity = dialogView.findViewById<TextInputEditText>(R.id.etQuantity)
        val etSellPrice = dialogView.findViewById<TextInputEditText>(R.id.etSellPrice)
        val tvSellDate = dialogView.findViewById<TextInputEditText>(R.id.tvSellDate)
        val rgPaymentMethod = dialogView.findViewById<RadioGroup>(R.id.rgPaymentMethod)
        val rbCash = dialogView.findViewById<RadioButton>(R.id.rbCash)
        val btnSubmitSell = dialogView.findViewById<Button>(R.id.btnSubmitSell)
//Значения по умолчанию в диалоге
        etSellPrice.setText(addNom.price)
        etQuantity.setText("1")
        rbCash.isChecked = true

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

            if (quantityInput.isNotBlank() && sellPrice.isNotBlank() && selectedPaymentMethodId != 0) {
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


