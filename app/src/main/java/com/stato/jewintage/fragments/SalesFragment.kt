package com.stato.jewintage.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.stato.jewintage.MainActivity
import com.stato.jewintage.databinding.FragmentSalesBinding
import com.stato.jewintage.viewmodel.SalesViewModel

class SalesFragment : Fragment() {
    private lateinit var viewModel: SalesViewModel
    private lateinit var salesAdapter: SalesAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSalesBinding.inflate(inflater, container, false)


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

        viewModel = ViewModelProvider(this).get(SalesViewModel::class.java)
        salesAdapter = SalesAdapter(act = MainActivity())

        binding.rcViewSales.layoutManager = LinearLayoutManager(context)
        binding.rcViewSales.adapter = salesAdapter
        updateUi()

        return binding.root
    }
    private fun updateUi() {
        viewModel.liveSalesData.observe(viewLifecycleOwner) { salesList ->
            salesAdapter.updateSales(salesList)
        }

        viewModel.loadAllSales()
    }
}

