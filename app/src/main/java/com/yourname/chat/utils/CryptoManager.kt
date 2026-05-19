package com.yourname.chat.utils

import android.content.Context
import android.util.Base64
import com.google.crypto.tink.BinaryKeysetReader
import com.google.crypto.tink.BinaryKeysetWriter
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.HybridDecrypt
import com.google.crypto.tink.HybridEncrypt
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.hybrid.HybridConfig
import com.google.crypto.tink.hybrid.HybridKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class CryptoManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEYSET_NAME = "master_keyset"
        private const val PREFERENCE_FILE = "crypto_prefs"
        private const val MASTER_KEY_URI = "android-keystore://master_key"
    }

    init {
        HybridConfig.register()
    }

    fun getPrivateKeyHandle(): KeysetHandle {
        val manager = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREFERENCE_FILE)
            .withKeyTemplate(HybridKeyTemplates.ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM)
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
        val handle = manager.keysetHandle
        return handle
    }

    fun getPublicKeyString(privateKeyHandle: KeysetHandle): String {
        val publicKeyHandle = privateKeyHandle.publicKeysetHandle
        val outputStream = ByteArrayOutputStream()
        CleartextKeysetHandle.write(
            publicKeyHandle,
            BinaryKeysetWriter.withOutputStream(outputStream)
        )
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    fun encrypt(plaintext: String, opponentPublicKeyBase64: String): String {
        val publicKeyData = Base64.decode(opponentPublicKeyBase64, Base64.NO_WRAP)
        val publicKeyHandle = CleartextKeysetHandle.read(
            BinaryKeysetReader.withBytes(publicKeyData)
        )

        val hybridEncrypt = publicKeyHandle.getPrimitive(HybridEncrypt::class.java)
        val ciphertext = hybridEncrypt.encrypt(plaintext.toByteArray(Charsets.UTF_8), null)

        return Base64.encodeToString(ciphertext, Base64.NO_WRAP)
    }

    fun decrypt(ciphertextBase64: String, privateKeyHandle: KeysetHandle): String {
       return try {
           val hybridDecrypt = privateKeyHandle.getPrimitive(HybridDecrypt::class.java)
           val ciphertext = Base64.decode(ciphertextBase64, Base64.NO_WRAP)
           val decrypted = hybridDecrypt.decrypt(ciphertext, null)
           String(decrypted, Charsets.UTF_8)
       } catch (e: Exception) {
           "Error"
       }
    }




}