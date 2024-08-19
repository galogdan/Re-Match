package com.example.re_match.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.re_match.databinding.FragmentMyFriendsBinding
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.ui.adapters.FriendsAdapter
import com.example.re_match.ui.viewmodels.DeleteFriendState
import com.example.re_match.ui.viewmodels.MyFriendsState
import com.example.re_match.ui.viewmodels.MyFriendsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyFriendsFragment : Fragment() {
    private val viewModel: MyFriendsViewModel by viewModels()
    private lateinit var binding: FragmentMyFriendsBinding
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        viewModel.getFriends()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        friendsAdapter = FriendsAdapter { friend ->
            showDeleteConfirmationDialog(friend)
        }
        binding.rvFriends.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendsAdapter
        }
    }

    private fun showDeleteConfirmationDialog(friend: UserProfile) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Friend")
            .setMessage("Are you sure you want to delete ${friend.nickname} from your friends list? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteFriend(friend.uid)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.friends.collect { friends ->
                friendsAdapter.submitList(friends)
                updateEmptyState(friends.isEmpty())
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MyFriendsState.Loading -> showLoading(true)
                is MyFriendsState.Success -> showLoading(false)
                is MyFriendsState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }

        viewModel.deleteFriendState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DeleteFriendState.Loading -> showLoading(true)
                is DeleteFriendState.Success -> {
                    showLoading(false)
                    showMessage("Friend deleted successfully")
                }
                is DeleteFriendState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.rvFriends.isVisible = !isEmpty
        binding.tvEmptyState.isVisible = isEmpty
    }

    private fun showMessage(message: String) {
        //Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingView.startLoading()
        } else {
            binding.loadingView.stopLoading()
        }
    }

    private fun showError(message: String) {
        //Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


}