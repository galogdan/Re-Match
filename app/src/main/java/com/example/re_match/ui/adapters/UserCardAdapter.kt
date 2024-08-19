package com.example.re_match.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.re_match.R
import com.example.re_match.databinding.ItemUserCardBinding
import com.example.re_match.domain.models.FriendRequestStatus
import com.example.re_match.domain.models.UserProfile

class UserCardAdapter(
    private val onSendOrCancelFriendRequest: (String) -> Unit
) : ListAdapter<UserProfile, UserCardAdapter.UserCardViewHolder>(UserDiffCallback()) {

    private var friendRequestStatuses: Map<String, FriendRequestStatus?> = emptyMap()

    fun updateFriendRequestStatuses(statuses: Map<String, FriendRequestStatus?>) {
        friendRequestStatuses = statuses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserCardViewHolder {
        val binding = ItemUserCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserCardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserCardViewHolder(private val binding: ItemUserCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserProfile) {
            binding.apply {
                tvNickname.text = user.nickname
                tvFullName.text = user.fullName.takeIf { user.privacySettings["showFullName"] == true } ?: "Name hidden"
                chipGender.text = user.gender.toString().takeIf { user.privacySettings["showGender"] == true } ?: "Gender hidden"
                chipAge.text = user.age.toString().takeIf{ user.privacySettings["showAge"] == true } ?: "Age hidden"
                chipPlatform.text = "${user.preferredPlatform.name}"
                tvShortDescription.text = user.shortDescription
                tvRegion.text = "Region: ${user.region}"
                tvGamingHours.text = "Gaming Hours: ${user.gamingHours}"
                tvPreferredGames.text = "Preferred Games: ${user.preferredGames.joinToString(", ")}"


                // load profile photo
                Glide.with(itemView.context)
                    .load(user.photoUrl)
                    .placeholder(R.drawable.default_profile_photo)
                    .into(ivProfilePhoto)

                val status = friendRequestStatuses[user.uid]
                btnSendRequest.text = when (status) {
                    FriendRequestStatus.PENDING -> "Cancel Request"
                    FriendRequestStatus.ACCEPTED -> "Friends"
                    FriendRequestStatus.REJECTED -> "Request Declined"
                    null -> "Send Friend Request"
                }

                btnSendRequest.isEnabled = status != FriendRequestStatus.ACCEPTED && status != FriendRequestStatus.REJECTED

                btnSendRequest.setOnClickListener {
                    onSendOrCancelFriendRequest(user.uid)
                }
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UserProfile>() {
        override fun areItemsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem == newItem
        }
    }
}