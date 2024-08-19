package com.example.re_match.domain.models

import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.Date

// message data class
data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String? = "",
    val content: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false
) {
    // empty constructor
    constructor() : this("", "", "", "", 0, false)
}