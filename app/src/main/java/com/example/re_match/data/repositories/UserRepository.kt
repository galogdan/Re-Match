package com.example.re_match.data.repositories

import android.util.Log
import com.example.re_match.domain.models.FriendRequest
import com.example.re_match.domain.models.FriendRequestStatus
import com.example.re_match.domain.models.GamingPlatform
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.domain.repositories.IUserRepository
import com.example.re_match.ui.viewmodels.DiscoverFilters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : IUserRepository {
    override suspend fun createUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            firestore.collection("users").document(userProfile.uid).set(userProfile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiscoveredUsers(filters: DiscoverFilters, currentUserId: String): Result<List<UserProfile>> {
        return try {
            Log.d("UserRepository", "Getting discovered users with filters: $filters")
            var query = applyFilters(firestore.collection("users"), filters)

            // exclude the current user
            query = query.whereNotEqualTo(FieldPath.documentId(), currentUserId)

            val snapshot = query.get().await()
            val users = snapshot.toObjects(UserProfile::class.java)
            Log.d("UserRepository", "Found ${users.size} users")
            Result.success(users)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting discovered users", e)
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String, filters: DiscoverFilters, currentUserId: String): Result<List<UserProfile>> {
        return try {
            var baseQuery = firestore.collection("users")
                .orderBy("nickname")
                .startAt(query)
                .endAt(query + '\uf8ff')
                .whereNotEqualTo(FieldPath.documentId(), currentUserId)

            // apply filters only if they are not empty
            if (!filters.region.isNullOrBlank()) {
                baseQuery = baseQuery.whereEqualTo("region", filters.region)
            }

            if (filters.preferredGames.isNotEmpty()) {
                baseQuery = baseQuery.whereArrayContainsAny("preferredGames", filters.preferredGames)
            }

            if (!filters.gamingHours.isNullOrBlank()) {
                baseQuery = baseQuery.whereEqualTo("gamingHours", filters.gamingHours)
            }

            if (filters.preferredPlatform != null && filters.preferredPlatform != GamingPlatform.ALL) {
                baseQuery = baseQuery.whereEqualTo("preferredPlatform", filters.preferredPlatform.name)
            }

            val snapshot = baseQuery.get().await()
            val users = snapshot.toObjects(UserProfile::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun applyFilters(query: Query, filters: DiscoverFilters): Query {
        var filteredQuery = query

        if (!filters.region.isNullOrBlank() && filters.region != "All Regions") {
            filteredQuery = filteredQuery.whereEqualTo("region", filters.region)
        }

        if (filters.preferredGames.isNotEmpty()) {
            filteredQuery = filteredQuery.whereArrayContainsAny("preferredGames", filters.preferredGames)
        }

        if (!filters.gamingHours.isNullOrBlank() && filters.gamingHours != "Any Time") {
            filteredQuery = filteredQuery.whereEqualTo("gamingHours", filters.gamingHours)
        }

        if (filters.preferredPlatform != null && filters.preferredPlatform != GamingPlatform.ALL) {
            filteredQuery = filteredQuery.whereEqualTo("preferredPlatform", filters.preferredPlatform.name)
        }

        return filteredQuery
    }
    override suspend fun sendFriendRequest(receiverId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val friendRequest = FriendRequest(
                senderId = currentUserId,
                receiverId = receiverId
            )
            firestore.collection("friendRequests").add(friendRequest).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFriendRequests(): Result<List<FriendRequest>> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val snapshot = firestore.collection("friendRequests")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", FriendRequestStatus.PENDING.name)
                .get()
                .await()
            val friendRequests = snapshot.toObjects(FriendRequest::class.java)
            Result.success(friendRequests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun respondToFriendRequest(requestId: String, accept: Boolean): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val requestDoc = firestore.collection("friendRequests").document(requestId)

            firestore.runTransaction { transaction ->
                val request = transaction.get(requestDoc).toObject(FriendRequest::class.java)
                    ?: throw Exception("Friend request not found")

                if (request.receiverId != currentUserId) {
                    throw Exception("Unauthorized action")
                }

                if (accept) {
                    // add users to each other friends sub collection
                    val currentUserFriendsCollection = firestore.collection("users").document(currentUserId).collection("friends")
                    val senderFriendsCollection = firestore.collection("users").document(request.senderId).collection("friends")

                    transaction.set(currentUserFriendsCollection.document(request.senderId), hashMapOf("userId" to request.senderId))
                    transaction.set(senderFriendsCollection.document(currentUserId), hashMapOf("userId" to currentUserId))

                    // update request status
                    transaction.update(requestDoc, "status", FriendRequestStatus.ACCEPTED.name)
                } else {
                    // delete request if declined
                    transaction.delete(requestDoc)
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFriends(): Result<List<UserProfile>> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val friendsSnapshot = firestore.collection("users")
                .document(currentUserId)
                .collection("friends")
                .get()
                .await()

            val friendIds = friendsSnapshot.documents.map { it.id }

            if (friendIds.isEmpty()) {
                // return an empty list if the user has no friends
                return Result.success(emptyList())
            }

            // perform the query if there are friend ids
            val friendProfiles = firestore.collection("users")
                .whereIn(FieldPath.documentId(), friendIds)
                .get()
                .await()
                .toObjects(UserProfile::class.java)

            Result.success(friendProfiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFriend(friendId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val currentUserFriendsCollection = firestore.collection("users").document(currentUserId).collection("friends")
            val friendFriendsCollection = firestore.collection("users").document(friendId).collection("friends")

            firestore.runTransaction { transaction ->
                transaction.delete(currentUserFriendsCollection.document(friendId))
                transaction.delete(friendFriendsCollection.document(currentUserId))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingFriendRequestsCount(): Result<Int> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val count = firestore.collection("friendRequests")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", FriendRequestStatus.PENDING.name)
                .count()
                .get(AggregateSource.SERVER)
                .await()
            Result.success(count.count.toInt())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelFriendRequest(receiverId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val requestQuery = firestore.collection("friendRequests")
                .whereEqualTo("senderId", currentUserId)
                .whereEqualTo("receiverId", receiverId)
                .whereEqualTo("status", FriendRequestStatus.PENDING.name)
                .limit(1)
                .get()
                .await()

            if (requestQuery.documents.isNotEmpty()) {
                val requestId = requestQuery.documents[0].id
                firestore.collection("friendRequests").document(requestId).delete().await()
            } else {
                throw Exception("Friend request not found")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkFriendRequestStatus(receiverId: String): Result<FriendRequestStatus?> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")

            // check if users are already friends
            val areFriends = isFriend(currentUserId, receiverId)
            if (areFriends) {
                return Result.success(FriendRequestStatus.ACCEPTED)
            }

            // check for friend request in both directions
            val requestQuery = firestore.collection("friendRequests")
                .whereIn("senderId", listOf(currentUserId, receiverId))
                .whereIn("receiverId", listOf(currentUserId, receiverId))
                .limit(1)
                .get()
                .await()

            val status = if (requestQuery.documents.isNotEmpty()) {
                requestQuery.documents[0].get("status") as? String
            } else {
                null
            }

            Result.success(status?.let { FriendRequestStatus.valueOf(it) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchFriends(currentUserId: String, query: String): Result<List<UserProfile>> {
        return try {
            val friendsSnapshot = firestore.collection("users")
                .document(currentUserId)
                .collection("friends")
                .get()
                .await()

            val friendIds = friendsSnapshot.documents.map { it.id }

            if (friendIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val lowercaseQuery = query.toLowerCase().trim()

            if (lowercaseQuery.isEmpty()) {
                // if query is empty, return all friends
                val allFriends = firestore.collection("users")
                    .whereIn(FieldPath.documentId(), friendIds)
                    .get()
                    .await()
                    .toObjects(UserProfile::class.java)
                return Result.success(allFriends)
            }

            // perform the query only if there are friend IDs and the query is not empty
            val matchingFriends = firestore.collection("users")
                .whereIn(FieldPath.documentId(), friendIds)
                .get()
                .await()
                .toObjects(UserProfile::class.java)
                .filter { friend ->
                    friend.nickname.toLowerCase().contains(lowercaseQuery) ||
                            friend.fullName.toLowerCase().contains(lowercaseQuery) ||
                            friend.email.toLowerCase().contains(lowercaseQuery)
                }

            Result.success(matchingFriends)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // checks if other user in friend list sub collection
    override suspend fun isFriend(currentUserId: String, potentialFriendId: String): Boolean {
        return try {
            val friendDoc = firestore.collection("users")
                .document(currentUserId)
                .collection("friends")
                .document(potentialFriendId)
                .get()
                .await()
            friendDoc.exists()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUserProfileById(userId: String): UserProfile {
        val document = firestore.collection("users").document(userId).get().await()
        return document.toObject(UserProfile::class.java) ?: throw Exception("User not found")
    }




}