package com.example.re_match.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.re_match.R
import com.example.re_match.databinding.ItemFriendRequestBinding
import com.example.re_match.domain.models.FriendRequest
import com.example.re_match.domain.models.UserProfile

class FriendRequestAdapter(
    private val onAccept: (String) -> Unit,
    private val onDecline: (String) -> Unit
) : ListAdapter<Pair<FriendRequest, UserProfile?>, FriendRequestAdapter.ViewHolder>(FriendRequestDiffCallback())  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemFriendRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Pair<FriendRequest, UserProfile?>) {
            val (friendRequest, userProfile) = item
            binding.tvSenderName.text = userProfile?.nickname ?: "Unknown"
            Glide.with(binding.root.context)
                .load(userProfile?.photoUrl)
                .placeholder(R.drawable.default_profile_photo)
                .into(binding.ivSenderPhoto)
            binding.btnAccept.setOnClickListener { onAccept(friendRequest.id) }
            binding.btnDecline.setOnClickListener { onDecline(friendRequest.id) }
        }
    }

    class FriendRequestDiffCallback : DiffUtil.ItemCallback<Pair<FriendRequest, UserProfile?>>() {
        override fun areItemsTheSame(oldItem: Pair<FriendRequest, UserProfile?>, newItem: Pair<FriendRequest, UserProfile?>): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(oldItem: Pair<FriendRequest, UserProfile?>, newItem: Pair<FriendRequest, UserProfile?>): Boolean {
            return oldItem == newItem
        }
    }
}