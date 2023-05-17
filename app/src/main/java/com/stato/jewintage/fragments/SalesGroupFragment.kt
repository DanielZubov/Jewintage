package com.stato.jewintage.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.stato.jewintage.adapters.SalesGroupAdapter
import com.stato.jewintage.databinding.FragmentSalesGroupBinding
import com.stato.jewintage.model.DbManager
import com.stato.jewintage.viewmodel.FirebaseViewModel

class SalesGroupFragment : Fragment(), SalesGroupAdapter.OnDateClickListener {
    private var _binding: FragmentSalesGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var salesGroupAdapter: SalesGroupAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private val dbManager = DbManager()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesGroupBinding.inflate(inflater, container, false)
        swipeRefreshLayout = binding.SalesGroupFrag

        swipeRefreshLayout.setOnRefreshListener {
            updateUi()
            swipeRefreshLayout.isRefreshing = false
        }

        binding.SalesGroupFrag.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        salesGroupAdapter = SalesGroupAdapter(this, dbManager)
        setupRecyclerView()
        updateUi()
    }

    private fun setupRecyclerView() {
        salesGroupAdapter = SalesGroupAdapter(object : SalesGroupAdapter.OnDateClickListener {
            override fun onDateClick(date: String) {
                val action =
                    SalesGroupFragmentDirections.actionSalesGroupFragmentToSalesFragment(date)
                findNavController().navigate(action)
            }
        }, dbManager)


        binding.rcViewDates.layoutManager = LinearLayoutManager(context)
        binding.rcViewDates.adapter = salesGroupAdapter
    }

    private fun updateUi() {
        firebaseViewModel.loadSalesGroupDates()
        firebaseViewModel.salesGroupList.observe(viewLifecycleOwner) { salesGroupList ->
            salesGroupAdapter.setData(salesGroupList)
        }
    }

    override fun onDateClick(date: String) {
        val action = SalesGroupFragmentDirections.actionSalesGroupFragmentToSalesFragment(date)
        findNavController().navigate(action)
    }
}
