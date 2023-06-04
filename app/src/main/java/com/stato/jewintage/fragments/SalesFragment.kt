package com.stato.jewintage.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.Group
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.stato.jewintage.MainActivity
import com.stato.jewintage.R
import com.stato.jewintage.adapters.CategoryFilterAdapter
import com.stato.jewintage.adapters.ImageAdapter
import com.stato.jewintage.adapters.SalesAdapter
import com.stato.jewintage.adapters.SalesGroupAdapter
import com.stato.jewintage.databinding.FragmentSalesBinding
import com.stato.jewintage.model.AddSales
import com.stato.jewintage.model.DbManager
import com.stato.jewintage.viewmodel.FirebaseViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SalesFragment : Fragment(), SalesAdapter.OnEditClickListener,
    SalesAdapter.OnDescriptionClickListener, SalesGroupAdapter.OnDateClickListener {
    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!
    private lateinit var salesAdapter: SalesAdapter
    private lateinit var categoryFilterAdapter: CategoryFilterAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var vpDes: ViewPager2
    private lateinit var adapter: ImageAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var isGetIntentFromMainActCalled = false
    private val dbManager = DbManager()
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args: SalesFragmentArgs by navArgs()
        selectedDate = args.selectedDate
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
        _binding = FragmentSalesBinding.inflate(inflater, container, false)

        swipeRefreshLayout = binding.SalesFrag

        swipeRefreshLayout.setOnRefreshListener {
            updateUi()
            firebaseViewModel.loadAllSales(selectedDate)
            swipeRefreshLayout.isRefreshing = false
        }

        binding.SalesFrag.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
        setupRecyclerView()
        updateUi()
        firebaseViewModel.loadAllSales(selectedDate)
        drawerLayout = binding.drawerLayoutSales
        categoryFilterAdapter = CategoryFilterAdapter(firebaseViewModel) { _, _ ->
            // Здесь вы можете обрабатывать выбор категории
        }
        val categoriesRecyclerView = binding.drawerFilterSales.categoriesRecyclerView
        categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        categoriesRecyclerView.adapter = categoryFilterAdapter

        binding.drawerFilterSales.dateFromEditText.setOnClickListener {
            showDatePickerDialog(binding.drawerFilterSales.dateFromEditText)
        }

        binding.drawerFilterSales.dateToEditText.setOnClickListener {
            showDatePickerDialog(binding.drawerFilterSales.dateToEditText)
        }
        binding.drawerFilterSales.applyFiltersButton.setOnClickListener {
            applyFilters()
        }
        binding.drawerFilterSales.resetButton.setOnClickListener {
            binding.drawerFilterSales.minPriceEditText.setText("")
            binding.drawerFilterSales.maxPriceEditText.setText("")
            binding.drawerFilterSales.dateFromEditText.setText("")
            binding.drawerFilterSales.dateToEditText.setText("")
            categoryFilterAdapter.resetCheckedCategories()
        }
        setupGroupsVisibility()
        setupFilterItemClickListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        val args = SalesFragmentArgs.fromBundle(requireArguments())
        // Загрузка и фильтрация данных с использованием selectedDate
        val selectedDate = args.selectedDate
        firebaseViewModel.filterSalesByDate(selectedDate)

        // Обновление RecyclerView при изменении данных
        firebaseViewModel.liveSalesData.observe(viewLifecycleOwner) { salesList ->
            salesAdapter.setData(salesList)
        }
        firebaseViewModel.loadAllSales(selectedDate)

    }
    override fun onDateClick(date: String) {
        selectedDate = date
        firebaseViewModel.loadAllSales(selectedDate)
    }
    private fun setupRecyclerView(){
        salesAdapter = SalesAdapter(requireActivity() as MainActivity, this, this)

        binding.rcViewSales.layoutManager = LinearLayoutManager(context)
        binding.rcViewSales.adapter = salesAdapter
    }

    private fun applyFilters() {
        drawerLayout.closeDrawer(GravityCompat.END)
        val selectedCategories = categoryFilterAdapter.checkedCategories
        val minPrice = binding.drawerFilterSales.minPriceEditText.text.toString().toDoubleOrNull()
        val maxPrice = binding.drawerFilterSales.maxPriceEditText.text.toString().toDoubleOrNull()
        val dateFrom = binding.drawerFilterSales.dateFromEditText.text.toString().takeIf { it.isNotEmpty() }
        val dateTo = binding.drawerFilterSales.dateToEditText.text.toString().takeIf { it.isNotEmpty() }

        firebaseViewModel.filterSalesAds(selectedCategories, minPrice, maxPrice, dateFrom, dateTo)
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
        binding.drawerFilterSales.categoriesGroup.visibility = View.GONE
        binding.drawerFilterSales.priceGroup.visibility = View.GONE
        binding.drawerFilterSales.dateGroup.visibility = View.GONE
    }
    private fun setupFilterItemClickListeners() {
        binding.drawerFilterSales.categoriesTitle.setOnClickListener {
            toggleGroupVisibility(binding.drawerFilterSales.categoriesGroup)
        }

        binding.drawerFilterSales.priceTitle.setOnClickListener {
            toggleGroupVisibility(binding.drawerFilterSales.priceGroup)
        }

        binding.drawerFilterSales.dateTitle.setOnClickListener {
            toggleGroupVisibility(binding.drawerFilterSales.dateGroup)
        }
    }
    private fun toggleGroupVisibility(group: Group) {
        group.visibility = if (group.visibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
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
                    salesAdapter.filter(newText)
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
    private fun getIntentFromMainAct(sale: AddSales) {
        if (isGetIntentFromMainActCalled) {
            return
        }
        isGetIntentFromMainActCalled = true
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            dbManager.getImagesFromDatabase(uid, sale.id!!) {
                activity?.runOnUiThread {
                    val imageUri = Uri.parse(sale.mainImage)
                    adapter = ImageAdapter(requireContext(), imageUri)
                    vpDes.adapter = adapter
                }
            }
        }
    }
    private fun updateUi() {
        firebaseViewModel.liveSalesData.observe(viewLifecycleOwner) {
            salesAdapter.setData(it)
        }
        firebaseViewModel.loadAllSales(selectedDate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onEditClick(sale: AddSales) {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_sell, null)

        val etSellPrice = dialogView.findViewById<TextInputEditText>(R.id.etSellPrice)
        val tvSellDate = dialogView.findViewById<TextInputEditText>(R.id.tvSellDate)
        val etQuantity = dialogView.findViewById<TextInputEditText>(R.id.etQuantity)
        val rbCash = dialogView.findViewById<RadioButton>(R.id.rbCash)
        val rbCard = dialogView.findViewById<RadioButton>(R.id.rbCard)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSubmitSave)

        // Заполните поля значениями текущего объекта продажи
        etSellPrice.setText(sale.price)
        tvSellDate.setText(sale.date)
        etQuantity.setText(sale.soldQuantity)

        // Устанавливаем выбранный метод оплаты
        if (sale.paymentMethod == "Наличка") {
            rbCash.isChecked = true
        } else {
            rbCard.isChecked = true
        }

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        builder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        builder.show()

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

        btnSave.setOnClickListener {
            // Получите измененные значения из виджетов
            val updatedSellPrice = etSellPrice.text.toString()
            val updatedSellDate = tvSellDate.text.toString()
            val updatedQuantity = etQuantity.text.toString()
            val paymentMethod = if (rbCash.isChecked) "Наличка" else "Visa/MasterCard"

            // Создайте экземпляр DbManager и обновите данные
            val dbManager = DbManager()
            dbManager.updateSale(
                sale,
                updatedSellPrice,
                updatedSellDate,
                updatedQuantity,
                paymentMethod,
                object : DbManager.FinishWorkListener {
                    override fun onFinish(isDone: Boolean) {
                        if (isDone) {
                            Toast.makeText(
                                requireActivity(),
                                "Продажа успешно обновлена",
                                Toast.LENGTH_SHORT
                            ).show()
                            updateUi() // Обновите UI, чтобы отобразить новые данные
                        } else {
                            Toast.makeText(
                                requireActivity(),
                                "Ошибка обновления продажи",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
            builder.dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onDescriptionClick(sale: AddSales) {
        isGetIntentFromMainActCalled = false
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_description_sale, null)

        val tvSum = dialogView.findViewById<TextView>(R.id.tvSum)
        val tvPrice = dialogView.findViewById<TextView>(R.id.tvDesPrice)
        val tvCat = dialogView.findViewById<TextView>(R.id.tvDesCat)
        val tvDesc = dialogView.findViewById<TextView>(R.id.tvDesDesc)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvDesDate)
        val tvQuantity = dialogView.findViewById<TextView>(R.id.tvDesQuantity)
        val tvPay = dialogView.findViewById<TextView>(R.id.tvDesPayMethod)
        val tvPercent = dialogView.findViewById<TextView>(R.id.tvDesComission)
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            decimalSeparator = '.'
        }
        val df = DecimalFormat("#.##", symbols)

        val newPercent = df.format((sale.price!!.toFloat() * sale.soldQuantity!!.toFloat()) - sale.sum!!.toFloat())

        // Заполните поля значениями текущего объекта продажи
        tvCat.text = sale.category
        tvPrice.text = sale.price
        tvDesc.text = sale.description
        "${sale.soldQuantity} шт.".also { tvQuantity.text = it }
        "₾ ${sale.sum}".also { tvSum.text = it }
        tvDate.text = sale.date
        tvPay.text = sale.paymentMethod
        tvPercent.text = "₾ $newPercent"

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = builder.create()

        vpDes = dialogView.findViewById(R.id.vpDesSale)
        getIntentFromMainAct(sale)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

}
