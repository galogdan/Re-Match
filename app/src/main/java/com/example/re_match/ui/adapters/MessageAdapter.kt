package com.example.re_match.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.re_match.R
import com.example.re_match.databinding.ItemMessageBinding
import com.example.re_match.domain.models.Message
import com.example.re_match.utils.DateUtils

class MessageAdapter(private val currentUserId: String) :
    ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position), currentUserId)
    }

    class MessageViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message, currentUserId: String) {
            binding.tvMessageContent.text = message.content
            binding.tvTimestamp.text = DateUtils.formatRelativeTimestamp(message.timestamp)

            val isCurrentUser = message.senderId == currentUserId
            val layoutParams = binding.tvMessageContent.layoutParams as ConstraintLayout.LayoutParams

            if (isCurrentUser) {
                binding.tvMessageContent.setBackgroundResource(R.drawable.bg_sent_message)
                binding.tvMessageContent.setTextColor(ContextCompat.getColor(itemView.context, R.color.sent_message_text))
                layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET
                layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                (binding.tvTimestamp.layoutParams as ConstraintLayout.LayoutParams).apply {
                    startToStart = ConstraintLayout.LayoutParams.UNSET
                    endToEnd = binding.tvMessageContent.id
                }
            } else {
                binding.tvMessageContent.setBackgroundResource(R.drawable.bg_received_message)
                binding.tvMessageContent.setTextColor(ContextCompat.getColor(itemView.context, R.color.received_message_text))
                layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
                (binding.tvTimestamp.layoutParams as ConstraintLayout.LayoutParams).apply {
                    startToStart = binding.tvMessageContent.id
                    endToEnd = ConstraintLayout.LayoutParams.UNSET
                }
            }

            binding.tvMessageContent.layoutParams = layoutParams
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}