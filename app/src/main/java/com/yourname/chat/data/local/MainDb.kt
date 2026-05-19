package com.yourname.chat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.sqlcipher.database.SupportFactory
import java.util.UUID

@Database(
    entities = [MessageEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MainDb : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: MainDb? = null

        fun getDatabase(context: Context): MainDb {

            return INSTANCE ?: synchronized(this) {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val sharedPrefs = EncryptedSharedPreferences.create(
                    context,
                    "sqlcipher_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )

                var passphrase = sharedPrefs.getString("db_passphrase", null)
                if(passphrase==null) {
                    passphrase = UUID.randomUUID().toString()
                    sharedPrefs.edit().putString("db_passphrase", passphrase).apply()
                }

                val passphraseBytes = passphrase.toByteArray()
                val factory = SupportFactory(passphraseBytes)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MainDb::class.java,
                    "messages.db"
                ).openHelperFactory(factory)
                    .build()

                passphraseBytes.fill(0)

                INSTANCE = instance
                instance
            }
        }
    }
}