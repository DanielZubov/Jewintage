package com.stato.jewintage.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.stato.jewintage.EditItemCost
import com.stato.jewintage.MainActivity
import com.stato.jewintage.adapters.CostGroupAdapter
import com.stato.jewintage.databinding.FragmentCostGroupBinding
import com.stato.jewintage.model.DbManager
import com.stato.jewintage.viewmodel.FirebaseViewModel

class CostGroupFragment : Fragment(), CostGroupAdapter.OnDateClickListener {
    private var _binding: FragmentCostGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var costGroupAdapter: CostGroupAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private val dbManager = DbManager()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCostGroupBinding.inflate(inflater, container, false)
        swipeRefreshLayout = binding.CostGroupFrag

        swipeRefreshLayout.setOnRefreshListener {
            updateUi()
            swipeRefreshLayout.isRefreshing = false
        }

        binding.CostGroupFrag.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        costGroupAdapter = CostGroupAdapter(this, dbManager)
        setupRecyclerView()
        updateUi()
    }
    private fun onClickAddNewCost() {
        val i = Intent(requireActivity() as MainActivity, EditItemCost::class.java)
        startActivity(i)
    }

    private fun setupRecyclerView() {
        costGroupAdapter = CostGroupAdapter(object : CostGroupAdapter.OnDateClickListener {
            override fun onDateClick(date: String) {
                val action =
                    CostGroupFragmentDirections.actionCostGroupFragmentToCostFragment(date)
                findNavController().navigate(action)
            }
        }, dbManager)


        binding.rcViewDates.layoutManager = LinearLayoutManager(context)
        binding.rcViewDates.adapter = costGroupAdapter
        binding.fbAddCost2.setOnClickListener {
            onClickAddNewCost()
        }
    }

    private fun updateUi() {
        firebaseViewModel.loadCostsGroupDates()
        firebaseViewModel.costsGroupList.observe(viewLifecycleOwner) { costsGroupList ->
            costGroupAdapter.setData(costsGroupList)
        }
    }

    override fun onDateClick(date: String) {
        val action = CostGroupFragmentDirections.actionCostGroupFragmentToCostFragment(date)
        findNavController().navigate(action)
    }
}
