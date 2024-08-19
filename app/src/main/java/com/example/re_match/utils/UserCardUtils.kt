package com.example.re_match.utils

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.re_match.R
import com.example.re_match.domain.models.UserProfile

// user card util
object UserCardUtil {
    fun populateUserCard(view: View, profile: UserProfile, showFriendRequestButton: Boolean = true) {
        with(view) {
            findViewById<ImageView>(R.id.ivProfilePhoto)?.apply {
                Glide.with(this)
                    .load(profile.photoUrl)
                    .placeholder(R.drawable.default_profile_photo)
                    .into(this)
            }
            findViewById<TextView>(R.id.tvNickname)?.text = profile.nickname
            findViewById<TextView>(R.id.tvFullName)?.text = profile.fullName.takeIf { profile.privacySettings["showFullName"] == true } ?: "Name hidden"
            findViewById<TextView>(R.id.chipGender)?.text = profile.gender.toString().takeIf { profile.privacySettings["showGender"] == true } ?: "Gender hidden"
            findViewById<TextView>(R.id.chipAge)?.text = profile.age?.toString().takeIf { profile.privacySettings["showAge"] == true } ?: "Age hidden"
            findViewById<TextView>(R.id.tvShortDescription)?.text = profile.shortDescription
            findViewById<TextView>(R.id.tvRegion)?.text = "Region: ${profile.region}"
            findViewById<TextView>(R.id.tvGamingHours)?.text = "Gaming Hours: ${profile.gamingHours}"
            findViewById<TextView>(R.id.tvPreferredGames)?.text = "Preferred Games: ${profile.preferredGames.joinToString(", ")}"
            findViewById<TextView>(R.id.chipPlatform)?.text = "Preferred Platform: ${profile.preferredPlatform}"

            findViewById<Button>(R.id.btnSendRequest)?.visibility = if (showFriendRequestButton) View.VISIBLE else View.GONE
        }
    }
}