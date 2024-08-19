package com.example.re_match.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.re_match.R
import com.example.re_match.databinding.ItemSearchResultBinding
import com.example.re_match.domain.models.UserProfile

class SearchResultAdapter(private val onUserClicked: (UserProfile) -> Unit) :
    ListAdapter<UserProfile, SearchResultAdapter.SearchResultViewHolder>(SearchResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultViewHolder(binding, onUserClicked)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SearchResultViewHolder(
        private val binding: ItemSearchResultBinding,
        private val onUserClicked: (UserProfile) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserProfile) {
            binding.tvUserName.text = user.nickname
            Glide.with(binding.root.context)
                .load(user.photoUrl)
                .placeholder(R.drawable.default_profile_photo)
                .error(R.drawable.default_profile_photo)
                .circleCrop()
                .into(binding.ivProfilePic)
            itemView.setOnClickListener { onUserClicked(user) }
        }
    }

    class SearchResultDiffCallback : DiffUtil.ItemCallback<UserProfile>() {
        override fun areItemsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem == newItem
        }
    }
}