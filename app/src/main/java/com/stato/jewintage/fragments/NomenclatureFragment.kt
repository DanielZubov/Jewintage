package com.stato.jewintage.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.stato.jewintage.MainActivity
import com.stato.jewintage.adapters.AddRcAdapter
import com.stato.jewintage.databinding.FragmentNomenclatureBinding
import com.stato.jewintage.viewmodel.FirebaseViewModel

class NomenclatureFragment : Fragment(){
    private var _binding: FragmentNomenclatureBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AddRcAdapter
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNomenclatureBinding.inflate(inflater, container, false)
        val view = binding.root

        // Настройте адаптер и RecyclerView
        setupRecyclerView()

        // Инициализация ViewModel и обновление адаптера
        initViewModel()
        firebaseViewModel.loadAllAds()
        return view
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


