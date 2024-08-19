package com.example.re_match.domain.models

import java.time.LocalDate
import java.time.Period
import java.util.Date
import java.util.concurrent.TimeUnit

// enum gender
enum class Gender {
    MALE, FEMALE, NOT_SPECIFIED
}

// enum gaming platform
enum class GamingPlatform {
    ALL, PC, PLAYSTATION, XBOX, NINTENDO, MOBILE, OTHER
}

data class UserProfile(
    val uid: String,
    val email: String,
    val fullName: String,
    val nickname: String,
    val gender: Gender,
    val photoUrl: String? = null,
    val preferredGames: List<String> = emptyList(),
    val preferredPlatform: GamingPlatform = GamingPlatform.OTHER,
    val gamingHours: String = "",
    val region: String = "",
    val shortDescription: String = "",
    val birthDateTimestamp: Long? = null,  // Store as Long instead of LocalDate
    val friendRequests: List<String> = emptyList(),
    @field:JvmField
    val isFirstLogin: Boolean = true,
    val privacySettings: Map<String, Boolean> = mapOf(
        "showFullName" to false,
        "showGender" to false,
        "showAge" to false
    )
) {
    val age: Int?
        get() = birthDateTimestamp?.let {
            val birthDate = Date(it)
            val today = Date()
            val diff = today.time - birthDate.time
            TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt() / 365
        }

    // empty constructor
    constructor() : this(
        uid = "",
        email = "",
        fullName = "",
        nickname = "",
        gender = Gender.NOT_SPECIFIED,
        photoUrl = null,
        preferredGames = emptyList(),
        preferredPlatform = GamingPlatform.OTHER,
        gamingHours = "",
        region = "",
        shortDescription = "",
        birthDateTimestamp = null,
        friendRequests = emptyList()
    )
}
