package com.example.re_match.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.re_match.R
import com.example.re_match.databinding.FragmentDiscoverBinding
import com.example.re_match.domain.models.GamingPlatform
import com.example.re_match.ui.adapters.UserCardAdapter
import com.example.re_match.ui.viewmodels.DiscoverViewModel
import com.example.re_match.ui.viewmodels.FriendRequestStatusState
import com.google.android.material.textfield.TextInputEditText
import com.example.re_match.ui.views.LoadingView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiscoverViewModel by viewModels()
    private lateinit var userCardAdapter: UserCardAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupUI()
        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        userCardAdapter = UserCardAdapter { userId ->
            viewModel.sendOrCancelFriendRequest(userId)
        }
        binding.rvDiscoveredUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userCardAdapter
        }
    }

    private fun setupUI() {
        binding.btnFilter.setOnClickListener { showFilterDialog() }
        (binding.etSearch).setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    Log.d("DiscoverFragment", "Search submitted with query: $it")
                    viewModel.searchUsers(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optionally implement real-time search here
                return true
            }
        })
    }

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_discover_filter, null)
        val spinnerRegion = dialogView.findViewById<Spinner>(R.id.spinnerRegion)
        val etPreferredGames = dialogView.findViewById<TextInputEditText>(R.id.etPreferredGames)
        val spinnerGamingHours = dialogView.findViewById<Spinner>(R.id.spinnerGamingHours)
        val spinnerPlatform = dialogView.findViewById<Spinner>(R.id.spinnerPlatform)  // Add this line

        // Populate spinners with data
        val regions = resources.getStringArray(R.array.gaming_regions)
        spinnerRegion.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, regions)

        val gamingHours = resources.getStringArray(R.array.gaming_hours)
        spinnerGamingHours.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, gamingHours)

        val platforms = listOf("All Platforms") + GamingPlatform.values().filter { it != GamingPlatform.ALL }.map { it.name }
        spinnerPlatform.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, platforms)

        AlertDialog.Builder(requireContext())
            .setTitle("Filter Users")
            .setView(dialogView)
            .setPositiveButton("Apply") { _, _ ->
                val selectedRegion = spinnerRegion.selectedItem.toString()
                val preferredGames = etPreferredGames.text.toString().split(",").map { it.trim() }
                val selectedGamingHours = spinnerGamingHours.selectedItem.toString()
                val selectedPlatform = when (spinnerPlatform.selectedItem.toString()) {
                    "All Platforms" -> GamingPlatform.ALL
                    else -> GamingPlatform.valueOf(spinnerPlatform.selectedItem.toString())
                }

                viewModel.applyFilters(
                    region = selectedRegion,
                    preferredGames = preferredGames,
                    gamingHours = selectedGamingHours,
                    preferredPlatform = selectedPlatform
                )
            }
            .setNegativeButton("Clear Filters") { _, _ ->
                viewModel.clearFilters()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun setupObservers() {
        viewModel.discoveredUsers.observe(viewLifecycleOwner) { users ->
            Log.d("DiscoverFragment", "Received ${users.size} users from ViewModel")
            userCardAdapter.submitList(users)
            viewModel.checkFriendRequestStatuses(users.map { it.uid })
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Log.e("DiscoverFragment", "Error message received: $errorMessage")
            //Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }

        viewModel.friendRequestStatusState.observe(viewLifecycleOwner) { status ->
            when (status) {
                is FriendRequestStatusState.Loading -> {
                    showLoading(true)
                }
                is FriendRequestStatusState.Success -> {
                    showLoading(false)
                    //Toast.makeText(context, "Friend request action successful", Toast.LENGTH_SHORT).show()
                }
                is FriendRequestStatusState.Error -> {
                    showLoading(false)
                    //Toast.makeText(context, "Error: ${status.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.friendRequestStatuses.observe(viewLifecycleOwner) { statuses ->
            userCardAdapter.updateFriendRequestStatuses(statuses)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingView.startLoading()
        } else {
            binding.loadingView.stopLoading()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDiscoveredUsers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}