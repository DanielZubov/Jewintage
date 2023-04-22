package com.stato.jewintage.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.stato.jewintage.MainActivity
import com.stato.jewintage.databinding.FragmentSalesBinding
import com.stato.jewintage.model.AddSales
import com.stato.jewintage.viewmodel.FirebaseViewModel

class SalesFragment : Fragment() {
    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!
    private lateinit var salesAdapter: SalesAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)

        swipeRefreshLayout = binding.SalesFrag

        swipeRefreshLayout.setOnRefreshListener {
            updateUi()
            swipeRefreshLayout.isRefreshing = false
        }

        binding.SalesFrag.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        salesAdapter = SalesAdapter(act = requireActivity() as MainActivity)

        binding.rcViewSales.layoutManager = LinearLayoutManager(context)
        binding.rcViewSales.adapter = salesAdapter
        updateUi()

        return binding.root
    }

    private fun updateUi() {
        firebaseViewModel.liveSalesData.observe(viewLifecycleOwner) { salesList ->
            salesAdapter.updateSales(salesList)
        }

        firebaseViewModel.loadAllSales()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
