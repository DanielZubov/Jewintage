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
import com.stato.jewintage.EditItemAct
import com.stato.jewintage.MainActivity
import com.stato.jewintage.adapters.NomGroupAdapter
import com.stato.jewintage.databinding.FragmentNomGroupBinding
import com.stato.jewintage.model.DbManager
import com.stato.jewintage.viewmodel.FirebaseViewModel

class NomGroupFragment : Fragment(), NomGroupAdapter.OnCategoryClickListener {
    private var _binding: FragmentNomGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var nomGroupAdapter: NomGroupAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private val dbManager = DbManager()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNomGroupBinding.inflate(inflater, container, false)
        swipeRefreshLayout = binding.NomGroupFrag

        swipeRefreshLayout.setOnRefreshListener {
            updateUi()
            swipeRefreshLayout.isRefreshing = false
        }

        binding.NomGroupFrag.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nomGroupAdapter = NomGroupAdapter(this, dbManager)
        setupRecyclerView()
        updateUi()
    }

    private fun setupRecyclerView() {
        nomGroupAdapter = NomGroupAdapter(object : NomGroupAdapter.OnCategoryClickListener {
            override fun onCategoryClick(category: String) {
                val action =
                    NomGroupFragmentDirections.actionNomGroupFragmentToNomenclatureFragment(category)
                findNavController().navigate(action)
            }
        }, dbManager)


        binding.rcViewDates.layoutManager = LinearLayoutManager(context)
        binding.rcViewDates.adapter = nomGroupAdapter
        binding.fbAddNom2.setOnClickListener {
            onClickAddNewNom()
        }
    }

    private fun updateUi() {
        firebaseViewModel.loadNomGroupCategory()
        firebaseViewModel.nomGroupList.observe(viewLifecycleOwner) { nomGroupList ->
            nomGroupAdapter.setData(nomGroupList)
        }
    }

    override fun onCategoryClick(category: String) {
        val action = NomGroupFragmentDirections.actionNomGroupFragmentToNomenclatureFragment(category)
        findNavController().navigate(action)
    }

    private fun onClickAddNewNom() {
        val i = Intent(requireActivity() as MainActivity, EditItemAct::class.java)
        startActivity(i)
    }
}
