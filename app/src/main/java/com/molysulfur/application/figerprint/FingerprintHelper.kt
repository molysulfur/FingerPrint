package com.molysulfur.application.figerprint

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.CancellationSignal
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat


class FingerprintHelper(private val activity: Activity) : FingerprintManager.AuthenticationCallback() {

    private var cancellationSignal: CancellationSignal? = null

    fun startAuth(
        manager: FingerprintManager,
        cryptoObject: FingerprintManager.CryptoObject
    ) {

        cancellationSignal = CancellationSignal()

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.USE_FINGERPRINT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null)
    }

    fun stopListening() {
        if (cancellationSignal != null) {
            cancellationSignal!!.cancel()
            cancellationSignal = null
        }
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        Log.e("FingerprintHelper", "onAuthenticationError:$errString")
    }


    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
        super.onAuthenticationHelp(helpCode, helpString)
        Toast.makeText(
            activity,
            "Authentication help\n$helpString",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        Toast.makeText(
            activity,
            "Authentication succeeded.",
            Toast.LENGTH_LONG
        ).show()
        val intent = Intent(activity,SecondActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        Toast.makeText(
            activity,
            "Authentication failed.",
            Toast.LENGTH_LONG
        ).show()
    }
}