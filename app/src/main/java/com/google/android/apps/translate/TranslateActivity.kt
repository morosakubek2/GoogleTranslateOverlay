package com.google.android.apps.translate

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast

class TranslateActivity : Activity() {
    companion object {
        private const val TAG = "TranslateOverlay"
        private const val TARGET_PACKAGE = "dev.davidv.translator"
        private const val TARGET_ACTIVITY = ".ProcessTextActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redukcja migotania – od razu przekieruj
        handleIntent(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val text: String?
        val newIntent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
            setComponent(ComponentName(TARGET_PACKAGE, TARGET_PACKAGE + TARGET_ACTIVITY))
            setType("text/plain")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)  // Bez migotania
        }

        when (action) {
            Intent.ACTION_PROCESS_TEXT -> {
                text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
                val readonly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)
                Log.d(TAG, "PROCESS_TEXT: $text, readonly=$readonly")
                newIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, text)
                if (readonly) newIntent.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
            }
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    text = intent.getStringExtra(Intent.EXTRA_TEXT)
                    Log.d(TAG, "SEND: $text")
                    newIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, text)
                } else return
            }
            else -> {
                text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: intent.getStringExtra(Intent.EXTRA_TEXT)
                Log.d(TAG, "Generic: $text")
                newIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, text)
            }
        }

        try {
            startActivity(newIntent)
            Log.d(TAG, "Redirected to $TARGET_PACKAGE")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start $TARGET_PACKAGE: ${e.message}")
            Toast.makeText(this, getString(R.string.error_cannot_open_translator), Toast.LENGTH_SHORT).show()
        }
    }
}
