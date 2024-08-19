package com.example.re_match.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.re_match.R
import com.example.re_match.databinding.FragmentChatBinding
import com.example.re_match.databinding.LayoutChatViewBinding
import com.example.re_match.ui.adapters.ChatListAdapter
import com.example.re_match.ui.adapters.MessageAdapter
import com.example.re_match.ui.adapters.SearchResultAdapter
import com.example.re_match.ui.viewmodels.ChatState
import com.example.re_match.ui.viewmodels.ChatViewModel
import com.example.re_match.utils.UserCardUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment() {


    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private var _chatViewBinding: LayoutChatViewBinding? = null
    private val chatViewBinding get() = _chatViewBinding!!

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var searchResultAdapter: SearchResultAdapter
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _chatViewBinding = LayoutChatViewBinding.inflate(layoutInflater, binding.chatContainer, true)
        setupMessageList()
        setupSearchView()
        setupChatList()
        setupSearchResultList()
        setupChatView()
        observeViewModel()

    }

    private fun setupMessageList() {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentUserId = viewModel.getCurrentUserId() ?: return@launch
            messageAdapter = MessageAdapter(currentUserId)
            chatViewBinding.rvMessages.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = messageAdapter
            }
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.startSearch()
                    viewModel.searchFriends(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.isEmpty()) {
                        viewModel.endSearch()
                        showChatList()
                    } else {
                        viewModel.startSearch()
                        viewModel.searchFriends(it)
                        showSearchResults()
                    }
                }
                return true
            }
        })
    }

    private fun setupChatList() {
        chatListAdapter = ChatListAdapter { chat ->
            viewModel.selectChat(chat)
            showChatView()
        }
        binding.rvChatList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatListAdapter
        }
    }

    private fun setupSearchResultList() {
        searchResultAdapter = SearchResultAdapter { user ->
            if (viewModel.isSearching.value) {
                viewModel.createOrGetChat(user.uid)
                showChatView()
            }
        }
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchResultAdapter
        }
    }

    private fun setupChatView() {


        chatViewBinding.btnBack.setOnClickListener {
            showChatList()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getCurrentUserId()?.let { currentUserId ->
                messageAdapter = MessageAdapter(currentUserId)
                chatViewBinding.rvMessages.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = messageAdapter
                }
            }
        }


        chatViewBinding.btnSend.setOnClickListener {
            val message = chatViewBinding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                chatViewBinding.etMessage.text.clear()
            }
        }

        viewModel.chatPartnerProfile.observe(viewLifecycleOwner) { profile ->
            chatViewBinding.tvChatTitle.text = profile.nickname
            Glide.with(this)
                .load(profile.photoUrl)
                .placeholder(R.drawable.default_profile_photo)
                .into(chatViewBinding.ivChatPartnerPhoto)
        }

        chatViewBinding.ivChatPartnerPhoto.setOnClickListener { showChatPartnerProfile() }
        chatViewBinding.tvChatTitle.setOnClickListener { showChatPartnerProfile() }


    }

    private fun showChatPartnerProfile() {
        val profile = viewModel.chatPartnerProfile.value ?: return
        val dialogView = layoutInflater.inflate(R.layout.dialog_user_card, null)

        UserCardUtil.populateUserCard(dialogView, profile, showFriendRequestButton = false)

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogStyle)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnCloseDialog).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showChatList() {
        binding.rvChatList.visibility = View.VISIBLE
        binding.rvSearchResults.visibility = View.GONE
        binding.chatContainer.visibility = View.GONE
        binding.searchView.visibility = View.VISIBLE
        viewModel.refreshChats() // Refresh chats when showing the chat list
    }

    private fun showSearchResults() {
        binding.rvChatList.visibility = View.GONE
        binding.rvSearchResults.visibility = View.VISIBLE
        binding.chatContainer.visibility = View.GONE
        binding.searchView.visibility = View.VISIBLE
    }

    private fun showChatView() {
        binding.rvChatList.visibility = View.GONE
        binding.rvSearchResults.visibility = View.GONE
        binding.chatContainer.visibility = View.VISIBLE
        binding.searchView.visibility = View.GONE
        binding.searchView.setQuery("", false) // Clear the search query
        binding.searchView.clearFocus()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.chats.collect { chatsWithProfiles ->
                        chatListAdapter.submitList(chatsWithProfiles)
                    }
                }
                launch {
                    viewModel.searchResults.collect { searchResults ->

                        searchResultAdapter.submitList(searchResults)
                        if (searchResults.isEmpty()) {
                            Log.d("ChatFragment", "No search results found")
                            // Consider showing a "No results" message here
                        }
                    }
                }
                launch {
                    viewModel.messages.collect { messages ->
                        messageAdapter.submitList(messages)
                        chatViewBinding.rvMessages.scrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        viewModel.selectedChat.observe(viewLifecycleOwner) { selectedChat ->
            selectedChat?.let { (chat, profile) ->
                chatViewBinding.tvChatTitle.text = profile?.nickname ?: "Chat"
                viewModel.refreshMessages(chat.id)
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->

            when (state) {
                is ChatState.Loading -> showLoading(true)
                is ChatState.ChatsRefreshed -> showLoading(false)
                is ChatState.MessagesRefreshed -> showLoading(false)
                is ChatState.SearchCompleted -> {
                    showLoading(false)


                    showSearchResults()
                }
                is ChatState.ChatSelected -> {
                    showLoading(false)
                    showChatView()
                }
                is ChatState.MessageSent -> {
                    showLoading(false)
                    chatViewBinding.etMessage.text?.clear()
                }
                is ChatState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }
    }



    private fun showLoading(isLoading: Boolean) {
    }

    private fun showError(message: String) {
        //Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _chatViewBinding = null
    }



}