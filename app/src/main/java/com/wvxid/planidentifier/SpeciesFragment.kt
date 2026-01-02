package com.wvxid.planidentifier

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wvxid.planidentifier.databinding.FragmentSpeciesBinding

class SpeciesFragment : Fragment() {

    private var _binding: FragmentSpeciesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeciesBinding.inflate(inflater, container, false)

        binding.herbsButton.setOnClickListener { openSpeciesList("herbs") }
        binding.shrubsButton.setOnClickListener { openSpeciesList("shrubs") }
        binding.treesButton.setOnClickListener { openSpeciesList("trees") }
        binding.climbersButton.setOnClickListener { openSpeciesList("climbers") }
        binding.creepersButton.setOnClickListener { openSpeciesList("creepers") }

        return binding.root
    }

    private fun openSpeciesList(category: String) {
        val intent = Intent(requireContext(), SpeciesListActivity::class.java)
        intent.putExtra("CATEGORY", category)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}