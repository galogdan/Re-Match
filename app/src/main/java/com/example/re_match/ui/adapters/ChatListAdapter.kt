package com.example.re_match.ui.adapters

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.re_match.R
import com.example.re_match.databinding.ItemChatBinding
import com.example.re_match.domain.models.Chat
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.utils.DateUtils

class ChatListAdapter(private val onChatClicked: (Chat) -> Unit) :
    ListAdapter<Pair<Chat, UserProfile?>, ChatListAdapter.ChatViewHolder>(ChatDiffCallback()) {

    private var searchQuery: String = ""

    fun setSearchQuery(query: String) {
        searchQuery = query.toLowerCase()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding, onChatClicked)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position), searchQuery)
    }

    class ChatViewHolder(
        private val binding: ItemChatBinding,
        private val onChatClicked: (Chat) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Pair<Chat, UserProfile?>, searchQuery: String) {
            val (chat, userProfile) = item
            val nickname = userProfile?.nickname ?: "Unknown"

            binding.tvNickname.text = nickname

            if (searchQuery.isNotEmpty() && nickname.toLowerCase().contains(searchQuery)) {
                val spannable = SpannableString(nickname)
                val startIndex = nickname.toLowerCase().indexOf(searchQuery)
                val endIndex = startIndex + searchQuery.length
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(itemView.context, R.color.highlight_color)),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.tvNickname.text = spannable
            } else {
                binding.tvNickname.text = nickname
            }

            Glide.with(binding.root.context)
                .load(userProfile?.photoUrl)
                .placeholder(R.drawable.default_profile_photo)
                .into(binding.ivProfilePic)

            binding.tvLastMessage.text = chat.lastMessage.takeIf { it.isNotEmpty() } ?: "No messages yet"
            binding.tvTimestamp.text = DateUtils.formatRelativeTimestamp(chat.lastMessageTimestamp)

            itemView.setOnClickListener { onChatClicked(chat) }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Pair<Chat, UserProfile?>>() {
        override fun areItemsTheSame(oldItem: Pair<Chat, UserProfile?>, newItem: Pair<Chat, UserProfile?>): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(oldItem: Pair<Chat, UserProfile?>, newItem: Pair<Chat, UserProfile?>): Boolean {
            return oldItem == newItem
        }
    }
}