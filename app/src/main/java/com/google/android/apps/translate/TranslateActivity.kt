package com.google.android.apps.translate

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.apps.translate.R

class TranslateActivity : Activity() {
    companion object {
        private const val TAG = "TranslateOverlay"
        private const val TARGET_PACKAGE = "dev.davidv.translator"
        private const val TARGET_ACTIVITY = ".ProcessTextActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        finish() // Zamknij aktywność po przekierowaniu
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val text: String?
        val newIntent = Intent(Intent.ACTION_PROCESS_TEXT)
        newIntent.setComponent(ComponentName(TARGET_PACKAGE, TARGET_PACKAGE + TARGET_ACTIVITY))
        newIntent.setType("text/plain")

        // Obsługa PROCESS_TEXT
        if (Intent.ACTION_PROCESS_TEXT == action) {
            text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
            val readonly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)
            Log.d(TAG, "Received PROCESS_TEXT: text=$text, readonly=$readonly")
            newIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, text)
            if (readonly) {
                newIntent.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
            }
        }
        // Obsługa ACTION_SEND
        else if (Intent.ACTION_SEND == action && "text/plain" == intent.type) {
            text = intent.getStringExtra(Intent.EXTRA_TEXT)
            Log.d(TAG, "Received SEND: text=$text")
            newIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, text)
        }
        // Obsługa jawnych Intentów do TranslateActivity
        else {
            text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: intent.getStringExtra(Intent.EXTRA_TEXT)
            Log.d(TAG, "Received generic intent: text=$text, action=$action, component=${intent.component}")
            newIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, text)
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
