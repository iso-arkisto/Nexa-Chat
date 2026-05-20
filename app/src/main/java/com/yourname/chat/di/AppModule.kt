package com.yourname.chat.di

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.yourname.chat.data.local.MainDb
import com.yourname.chat.data.local.MessageDao
import com.yourname.chat.domain.repository.AuthRepository
import com.yourname.chat.data.repository.AuthRepositoryImpl
import com.yourname.chat.domain.repository.MessageRepository
import com.yourname.chat.data.repository.MessageRepositoryImpl
import com.yourname.chat.domain.repository.UserRepository
import com.yourname.chat.data.repository.UserRepositoryImpl
import com.yourname.chat.utils.CryptoManager
import com.yourname.chat.utils.ServerTimeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent:: class)
object AppModule {
    @Provides
    fun provideMessageDao(database: MainDb): MessageDao {
        return database.messageDao()
    }

    @Provides
    @Singleton
    fun provideServerTimeManager(): ServerTimeManager {
        return ServerTimeManager
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideDatabase(): FirebaseDatabase = Firebase.database

    @Provides
    @Singleton
    fun provideMainDb(@ApplicationContext context: Context): MainDb = MainDb.getDatabase(context)

    @Provides
    @Singleton
    fun provideCryptoManager(@ApplicationContext context: Context): CryptoManager = CryptoManager(context)
    @Provides
    @Singleton
    fun provideMessageRepository(
        dao: MessageDao,
        cryptoManager: CryptoManager,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): MessageRepository = MessageRepositoryImpl(dao, firestore, cryptoManager, auth)

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        database: FirebaseDatabase
    ): UserRepository = UserRepositoryImpl(firestore, auth, database)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        cryptoManager: CryptoManager
    ): AuthRepository = AuthRepositoryImpl(auth, firestore, cryptoManager)
}