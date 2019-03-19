package com.molysulfur.application.figerprint

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import android.security.keystore.KeyPermanentlyInvalidatedException
import java.io.IOException
import java.security.*
import javax.crypto.SecretKey
import javax.security.cert.CertificateException


class MainActivity : AppCompatActivity() {

    private val KEY_NAME = "key_name"
    private val ANDROID_KEY_STORE = "AndroidKeyStore"

    private lateinit var mKeyStore: KeyStore
    private lateinit var mKeyGenerator: KeyGenerator
    private lateinit var mFingerprintManager: FingerprintManager
    private lateinit var mKeyguardManager: KeyguardManager
    private lateinit var cipher: Cipher
    private lateinit var mCryptoObject : FingerprintManager.CryptoObject
    private lateinit var mFingerprintHelper : FingerprintHelper

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mKeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        mFingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager

        if (mFingerprintManager.isHardwareDetected) {
            if (mKeyguardManager.isDeviceSecure) {
                if (mFingerprintManager.hasEnrolledFingerprints()) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) ==
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        generateKey()
                        if (initCipher()) {
                            mCryptoObject = FingerprintManager.CryptoObject(cipher)
                            mFingerprintHelper = FingerprintHelper(this)
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Fingerprint authentication permission not enabled",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Register at least one fingerprint in Settings",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Lock screen security not enabled in Settings",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mFingerprintHelper != null) {
            mFingerprintHelper.startAuth(mFingerprintManager, mCryptoObject)
        }
    }

    override fun onPause() {
        super.onPause()
        if (mFingerprintHelper != null) {
            mFingerprintHelper.stopListening()
        }
    }


    private fun generateKey() {
        try {
            mKeyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to get KeyGenerator instance", e
            )
        }
        try {
            mKeyStore.load(null)
            mKeyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
                    )
                    .build()
            )
            mKeyGenerator.generateKey()
        } catch (e: RuntimeException) {
            throw RuntimeException(e)
        }
    }

    private fun initCipher(): Boolean {
        try {
            cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7
            )
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }
        try {
            mKeyStore.load(null)
            val key = mKeyStore.getKey(KEY_NAME, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }


    }
}
