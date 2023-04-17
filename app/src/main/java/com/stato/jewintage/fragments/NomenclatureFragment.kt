package com.stato.jewintage.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.stato.jewintage.MainActivity
import com.stato.jewintage.R
import com.stato.jewintage.adapters.AddRcAdapter
import com.stato.jewintage.databinding.FragmentNomenclatureBinding
import com.stato.jewintage.viewmodel.FirebaseViewModel
import kotlin.random.Random

class NomenclatureFragment : Fragment(){
    private var _binding: FragmentNomenclatureBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AddRcAdapter
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNomenclatureBinding.inflate(inflater, container, false)
        val view = binding.root
        swipeRefreshLayout =  binding.nomenclatureFrag

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
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = AddRcAdapter(requireActivity() as MainActivity)
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




}


