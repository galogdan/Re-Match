package com.example.re_match.domain.models

import com.google.firebase.firestore.DocumentId
import java.util.Date

// friend request data class
enum class FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

data class FriendRequest(
    @DocumentId
    val id: String = "", // Unique identifier for the request
    val senderId: String, // User ID of the sender
    val receiverId: String, // User ID of the receiver
    val status: FriendRequestStatus = FriendRequestStatus.PENDING,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    // empty constructor
    constructor() : this("", "", "", FriendRequestStatus.PENDING, Date(), Date())
}