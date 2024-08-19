package com.example.re_match.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.re_match.R
import com.example.re_match.databinding.ItemFriendBinding
import com.example.re_match.domain.models.UserProfile

class FriendsAdapter(private val onDeleteClick: (UserProfile) -> Unit) :
    ListAdapter<UserProfile, FriendsAdapter.FriendViewHolder>(FriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FriendViewHolder(private val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: UserProfile) {
            binding.tvFriendName.text = friend.nickname
            Glide.with(binding.root.context)
                .load(friend.photoUrl)
                .placeholder(R.drawable.default_profile_photo)
                .into(binding.ivFriendPhoto)

            binding.btnRemoveFriend.setOnClickListener {
                onDeleteClick(friend)
            }
        }
    }

    class FriendDiffCallback : DiffUtil.ItemCallback<UserProfile>() {
        override fun areItemsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem == newItem
        }
    }
}