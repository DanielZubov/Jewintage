package com.stato.jewintage.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.stato.jewintage.adapters.SalesGroupAdapter
import com.stato.jewintage.databinding.FragmentSalesGroupBinding
import com.stato.jewintage.viewmodel.FirebaseViewModel

class SalesGroupFragment : Fragment() {
    private var _binding: FragmentSalesGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var salesGroupAdapter: SalesGroupAdapter
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesGroupBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        updateUi()
    }

    private fun setupRecyclerView() {
        salesGroupAdapter = SalesGroupAdapter(object : SalesGroupAdapter.OnDateClickListener {
            override fun onDateClick(date: String) {
                (requireActivity() as? OnSalesGroupDateSelectedListener)?.onSalesGroupDateSelected(date)
            }
        })

        binding.rcViewDates.layoutManager = LinearLayoutManager(context)
        binding.rcViewDates.adapter = salesGroupAdapter
    }

    private fun updateUi() {
        firebaseViewModel.loadSalesGroupDates()
        firebaseViewModel.salesGroupList.observe(viewLifecycleOwner) { salesGroupList ->
            salesGroupAdapter.setData(salesGroupList)
        }
    }

    interface OnSalesGroupDateSelectedListener {
        fun onSalesGroupDateSelected(date: String)
    }
}
