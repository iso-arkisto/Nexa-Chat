package com.yourname.chat.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yourname.chat.data.model.user.User
import com.yourname.chat.data.model.user.UserCore
import com.yourname.chat.domain.repository.AuthRepository
import com.yourname.chat.utils.CryptoManager
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cryptoManager: CryptoManager
): AuthRepository {
    override suspend fun signIn(email: String, pass: String): Result<Unit> = runCatching {
            auth.signInWithEmailAndPassword(email, pass).await()
            Unit
        }.onFailure { e ->
            Log.e("AuthRepository", "Failed to sign in user", e)
        }

    override suspend fun signUp(email: String, pass: String): Result<FirebaseUser> = runCatching {
            auth.createUserWithEmailAndPassword(email, pass).await()
            auth.signInWithEmailAndPassword(email, pass).await()

            val user = auth.currentUser ?: throw IllegalStateException("User not found after sign up")

            val privateKeyHandle = cryptoManager.getPrivateKeyHandle()
            val publicKeyString = cryptoManager.getPublicKeyString(privateKeyHandle)

            val userData = User(
                core = UserCore(
                    email = email,
                    uid = user.uid,
                    languageCode = Locale.getDefault().language ?: "en",
                    publicKey = publicKeyString
                )
            )

            firestore.collection("users")
                .document(user.uid)
                .set(userData)
                .await()

            user
        }.onFailure { e ->
            Log.e("AuthRepository", "Failed to sign up user", e)
            throw e
        }
}