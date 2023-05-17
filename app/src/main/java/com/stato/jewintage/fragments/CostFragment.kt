package com.stato.jewintage.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.Group
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.stato.jewintage.EditItemCost
import com.stato.jewintage.MainActivity
import com.stato.jewintage.R
import com.stato.jewintage.adapters.CategoryFilterAdapter
import com.stato.jewintage.adapters.CostAdapter
import com.stato.jewintage.adapters.ImageAdapter
import com.stato.jewintage.databinding.FragmentCostBinding
import com.stato.jewintage.model.AddCost
import com.stato.jewintage.model.DbManager
import com.stato.jewintage.util.ImageManager
import com.stato.jewintage.viewmodel.FirebaseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CostFragment : Fragment(), CostAdapter.OnEditClickListener, CostAdapter.OnDescriptionClickListener {
    private var _binding: FragmentCostBinding? = null
    private val binding get() = _binding!!
    private lateinit var costAdapter: CostAdapter
    private lateinit var categoryFilterAdapter: CategoryFilterAdapter
    private lateinit var vpDes: ViewPager2
    private lateinit var adapter: ImageAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var isGetIntentFromMainActCalled = false
    private val dbManager = DbManager()
    private lateinit var drawerLayout: DrawerLayout
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
        _binding = FragmentCostBinding.inflate(inflater, container, false)

        swipeRefreshLayout = binding.CostFrag

        swipeRefreshLayout.setOnRefreshListener {
            updateUi()
            firebaseViewModel.loadAllCost()
            swipeRefreshLayout.isRefreshing = false
        }

        binding.CostFrag.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
        setupRecyclerView()
        updateUi()
        firebaseViewModel.loadAllCost()
        drawerLayout = binding.drawerLayoutCost
        categoryFilterAdapter = CategoryFilterAdapter(firebaseViewModel) { _, _ ->
            // Здесь вы можете обрабатывать выбор категории
        }
        val categoriesRecyclerView = binding.drawerFilterCost.categoriesRecyclerView
        categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        categoriesRecyclerView.adapter = categoryFilterAdapter

        binding.drawerFilterCost.dateFromEditText.setOnClickListener {
            showDatePickerDialog(binding.drawerFilterCost.dateFromEditText)
        }

        binding.drawerFilterCost.dateToEditText.setOnClickListener {
            showDatePickerDialog(binding.drawerFilterCost.dateToEditText)
        }
        binding.drawerFilterCost.applyFiltersButton.setOnClickListener {
            applyFilters()
        }
        binding.drawerFilterCost.resetButton.setOnClickListener {
            binding.drawerFilterCost.minPriceEditText.setText("")
            binding.drawerFilterCost.maxPriceEditText.setText("")
            binding.drawerFilterCost.dateFromEditText.setText("")
            binding.drawerFilterCost.dateToEditText.setText("")
            categoryFilterAdapter.resetCheckedCategories()
        }


        setupGroupsVisibility()
        setupFilterItemClickListeners()
        return binding.root

    }

    private fun updateUi() {
        firebaseViewModel.liveCostData.observe(viewLifecycleOwner) {
            costAdapter.setData(it)
        }
        firebaseViewModel.loadAllCost()
    }

    private fun setupRecyclerView() {
        costAdapter = CostAdapter(act = requireActivity() as MainActivity, this)
        binding.rcViewCost.layoutManager = LinearLayoutManager(context)
        binding.rcViewCost.adapter = costAdapter
        binding.fbAddCost.setOnClickListener {
            onClickAddNewCost()
        }
    }

    private fun onClickAddNewCost() {
        val i = Intent(requireActivity() as MainActivity, EditItemCost::class.java)
        startActivity(i)
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

    private fun applyFilters() {
        drawerLayout.closeDrawer(GravityCompat.END)
        val selectedCategories = categoryFilterAdapter.checkedCategories
        val minPrice = binding.drawerFilterCost.minPriceEditText.text.toString().toDoubleOrNull()
        val maxPrice = binding.drawerFilterCost.maxPriceEditText.text.toString().toDoubleOrNull()
        val dateFrom =
            binding.drawerFilterCost.dateFromEditText.text.toString().takeIf { it.isNotEmpty() }
        val dateTo =
            binding.drawerFilterCost.dateToEditText.text.toString().takeIf { it.isNotEmpty() }

        firebaseViewModel.filterCostAds(selectedCategories, minPrice, maxPrice, dateFrom, dateTo)
    }

    private fun setupGroupsVisibility() {
        binding.drawerFilterCost.categoriesGroup.visibility = View.GONE
        binding.drawerFilterCost.priceGroup.visibility = View.GONE
        binding.drawerFilterCost.dateGroup.visibility = View.GONE
    }

    private fun setupFilterItemClickListeners() {
        binding.drawerFilterCost.categoriesTitle.setOnClickListener {
            toggleGroupVisibility(binding.drawerFilterCost.categoriesGroup)
        }

        binding.drawerFilterCost.priceTitle.setOnClickListener {
            toggleGroupVisibility(binding.drawerFilterCost.priceGroup)
        }

        binding.drawerFilterCost.dateTitle.setOnClickListener {
            toggleGroupVisibility(binding.drawerFilterCost.dateGroup)
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
                    costAdapter.filter(newText)
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

    private fun getIntentFromMainAct(cost: AddCost) {
        if (isGetIntentFromMainActCalled) {
            return
        }
        isGetIntentFromMainActCalled = true
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            dbManager.getImagesFromDatabase(uid, cost.id!!) {
                activity?.runOnUiThread {
                    updateVpAdapter(cost)
                }
            }
        }
    }


    private fun updateVpAdapter(cost: AddCost) {
        if (vpDes.adapter != adapter) {
            vpDes.adapter = adapter
        }
        ImageManager.fillImageCostArray(cost, adapter)
    }

    override fun onEditClick(cost: AddCost) {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_cost, null)

        val etPrice = dialogView.findViewById<TextInputEditText>(R.id.etCostPrice)
        val tvDate = dialogView.findViewById<TextInputEditText>(R.id.tvCostDate)
        val etQuantity = dialogView.findViewById<TextInputEditText>(R.id.etQuantity)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSubmitSave)
        val etCategory = dialogView.findViewById<TextInputEditText>(R.id.etCostCategory)
        val etCostDes = dialogView.findViewById<TextInputEditText>(R.id.etCostDes)

        // Заполните поля значениями текущего объекта продажи
        etPrice.setText(cost.price)
        tvDate.setText(cost.date)
        etQuantity.setText(cost.quantity)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = builder.show()

        // Слушатель клика на поле ввода даты
        tvDate.setOnClickListener {
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
                    tvDate.setText(selectedDateStr)
                },
                year,
                month,
                day
            )

            // Установка диапазона дат
            val minDate = Calendar.getInstance()
            minDate.set(2023, 0, 1)
            datePickerDialog.datePicker.minDate = minDate.timeInMillis

            // Показ диалога выбора даты
            datePickerDialog.show()
        }

        btnSave.setOnClickListener {
            // Получите измененные значения из виджетов
            val updatedSellPrice = etPrice.text.toString()
            val updatedSellDate = tvDate.text.toString()
            val updatedQuantity = etQuantity.text.toString()
            val updatedCostCategory = etCategory.text.toString()
            val updatedCostDes = etCostDes.text.toString()

            // Создайте экземпляр DbManager и обновите данные
            val dbManager = DbManager()
            dbManager.updateCost(
                cost,
                updatedSellPrice,
                updatedSellDate,
                updatedQuantity,
                updatedCostCategory,
                updatedCostDes,
                object : DbManager.FinishWorkListener {
                    override fun onFinish(isDone: Boolean) {
                        if (isDone) {
                            Toast.makeText(
                                requireActivity(),
                                "Элемент успешно обновлен",
                                Toast.LENGTH_SHORT
                            ).show()
                            updateUi() // Обновите UI, чтобы отобразить новые данные
                        } else {
                            Toast.makeText(
                                requireActivity(),
                                "Ошибка обновления",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })

            alertDialog.dismiss()
        }
    }

    override fun onDescriptionClick(cost: AddCost) {
        isGetIntentFromMainActCalled = false
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_description_cost, null)

        val tvSum = dialogView.findViewById<TextView>(R.id.tvSum)
        val tvCat = dialogView.findViewById<TextView>(R.id.tvDesCat)
        val tvDesc = dialogView.findViewById<TextView>(R.id.tvDesDesc)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvDesDate)
        val tvQuantity = dialogView.findViewById<TextView>(R.id.tvDesQuantity)

        // Заполните поля значениями текущего объекта продажи
        tvCat.text = cost.category
        tvDesc.text = cost.description
        "${cost.quantity} шт.".also { tvQuantity.text = it }
        "₾ ${cost.price}".also { tvSum.text = it }
        tvDate.text = cost.date

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = builder.create()

        vpDes = dialogView.findViewById(R.id.vpDes)
        adapter = ImageAdapter()
        vpDes.adapter = adapter
        getIntentFromMainAct(cost)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}