package com.stato.jewintage.fragments

import android.app.DatePickerDialog
import android.content.Intent
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
import android.widget.TextView
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
import com.stato.jewintage.EditItemCost
import com.stato.jewintage.MainActivity
import com.stato.jewintage.R
import com.stato.jewintage.adapters.CategoryFilterAdapter
import com.stato.jewintage.adapters.CostAdapter
import com.stato.jewintage.adapters.CostGroupAdapter
import com.stato.jewintage.adapters.ImageAdapter
import com.stato.jewintage.databinding.FragmentCostBinding
import com.stato.jewintage.model.AddCost
import com.stato.jewintage.model.DbManager
import com.stato.jewintage.viewmodel.FirebaseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CostFragment : Fragment(), CostAdapter.OnDescriptionClickListener, CostGroupAdapter.OnDateClickListener {
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
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args: CostFragmentArgs by navArgs()
        selectedDate = args.selectedDate
    }

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
            firebaseViewModel.loadAllCost(selectedDate)
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
        firebaseViewModel.loadAllCost(selectedDate)
        drawerLayout = binding.drawerLayoutCost
        categoryFilterAdapter = CategoryFilterAdapter(firebaseViewModel) { _, _ ->
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
        firebaseViewModel.loadAllCost(selectedDate)
    }

    private fun setupRecyclerView() {
        costAdapter = CostAdapter(requireActivity() as MainActivity, this)
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
                    val imageUri = Uri.parse(cost.mainImage)
                    adapter = ImageAdapter(requireContext(), imageUri)
                    vpDes.adapter = adapter
                }
            }
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
        getIntentFromMainAct(cost)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDateClick(date: String) {
        selectedDate = date
        firebaseViewModel.loadAllSales(selectedDate)
    }


}