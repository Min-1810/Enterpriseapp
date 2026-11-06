package com.fpt.enterpriseapp.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {
    private const val KEY = "FptEnterpriseSui"

    fun encrypt(data: String): String = try {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(KEY.toByteArray(), "AES"))
        Base64.encodeToString(cipher.doFinal(data.toByteArray()), Base64.DEFAULT)
    } catch (e: Exception) { data }

    fun decrypt(data: String): String = try {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(KEY.toByteArray(), "AES"))
        String(cipher.doFinal(Base64.decode(data, Base64.DEFAULT)))
    } catch (e: Exception) { data }
}