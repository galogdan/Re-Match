package com.example.re_match.ui.fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.re_match.databinding.FragmentFriendRequestsBinding
import com.example.re_match.ui.adapters.FriendRequestAdapter
import com.example.re_match.ui.viewmodels.FriendRequestViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendRequestsFragment : Fragment() {

    private var _binding: FragmentFriendRequestsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FriendRequestViewModel by viewModels()
    private lateinit var adapter: FriendRequestAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFriendRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupBackButton()

        viewModel.getFriendRequests()
    }

    private fun setupRecyclerView() {
        adapter = FriendRequestAdapter(
            onAccept = { requestId -> viewModel.respondToFriendRequest(requestId, true) },
            onDecline = { requestId -> viewModel.respondToFriendRequest(requestId, false) }
        )
        binding.rvFriendRequests.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.friendRequests.observe(viewLifecycleOwner) { requestsWithProfiles ->
            adapter.submitList(requestsWithProfiles)
        }

        viewModel.responseStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is FriendRequestViewModel.ResponseStatus.Success -> {
                    //Toast.makeText(context, "Request processed successfully", Toast.LENGTH_SHORT).show()
                    viewModel.getFriendRequests() // Refresh the list
                }
                is FriendRequestViewModel.ResponseStatus.Error -> {
                    //Toast.makeText(context, "Error: ${status.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingView.startLoading()
        } else {
            binding.loadingView.stopLoading()
        }
    }

    private fun setupBackButton() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}