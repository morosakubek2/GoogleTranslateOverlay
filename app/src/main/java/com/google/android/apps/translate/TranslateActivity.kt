package com.google.android.apps.translate

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast

class TranslateService : Service() {

    companion object {
        private const val TAG = "TranslateOverlay"
        private const val TARGET_PACKAGE = "dev.davidv.translator"
        private const val TARGET_ACTIVITY = ".ProcessTextActivity"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        stopSelf() // Zakończ usługę
        return START_NOT_STICKY
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val text: String?
        val newIntent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
            setComponent(ComponentName(TARGET_PACKAGE, TARGET_PACKAGE + TARGET_ACTIVITY))
            setType("text/plain")
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
                text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
                    ?: intent.getStringExtra(Intent.EXTRA_TEXT)
                Log.d(TAG, "Generic: $text")
                newIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, text)
            }
        }

        try {
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(newIntent)
            Log.d(TAG, "Started translator")
        } catch (e: Exception) {
            Log.e(TAG, "Failed: ${e.message}")
            Toast.makeText(this, "Nie można otworzyć tłumacza", Toast.LENGTH_SHORT).show()
        }
    }
}
