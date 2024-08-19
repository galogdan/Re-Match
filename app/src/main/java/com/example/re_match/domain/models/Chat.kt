package com.example.re_match.domain.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

// chat data class
data class Chat(
    val id: String = "",
    val participants: Map<String, Boolean> = mapOf(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0,
    val unreadCount: Int = 0
) {
    // empty constructor
    constructor() : this("", mapOf(), "", 0, 0)
}