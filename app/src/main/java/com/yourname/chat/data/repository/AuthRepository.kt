package com.yourname.chat.data.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun signIn(email: String, pass: String): Result<Unit>
    suspend fun signUp(email: String, pass: String): Result<FirebaseUser>
}