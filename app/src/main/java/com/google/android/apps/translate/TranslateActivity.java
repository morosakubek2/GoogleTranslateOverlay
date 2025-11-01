package com.google.android.apps.translate;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

public class TranslateActivity extends Activity {

    private static final String OFFLINE_PACKAGE = "dev.davidv.translator";
    private static final String OFFLINE_MAIN_ACTIVITY = ".MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start PreloadService to keep Offline-Translator in memory
        Intent preloadIntent = new Intent(this, PreloadService.class);
        startForegroundService(preloadIntent);

        // Handle incoming intent and redirect to Offline-Translator
        Intent incomingIntent = getIntent();
        if (incomingIntent != null) {
            try {
                redirectToOffline(incomingIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Error redirecting to translator", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No text to translate", Toast.LENGTH_SHORT).show();
        }
        finish(); // Close overlay activity after redirect
    }

    private void redirectToOffline(Intent incomingIntent) {
        Intent offlineIntent = new Intent(incomingIntent); // Copy original intent
        offlineIntent.setComponent(new ComponentName(OFFLINE_PACKAGE, OFFLINE_PACKAGE + OFFLINE_MAIN_ACTIVITY));

        // Handle different actions
        String action = incomingIntent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            // Sharing text
            if ("text/plain".equals(incomingIntent.getType())) {
                String sharedText = incomingIntent.getStringExtra(Intent.EXTRA_TEXT);
                if (!TextUtils.isEmpty(sharedText)) {
                    offlineIntent.putExtra(Intent.EXTRA_TEXT, sharedText);
                }
            }
            // Sharing image
            else if (incomingIntent.getType() != null && incomingIntent.getType().startsWith("image/")) {
                Uri imageUri = incomingIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    offlineIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                }
            }
        } else if ("android.intent.action.TRANSLATE".equals(action)) {
            // Direct translate action
            String textToTranslate = incomingIntent.getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(textToTranslate)) {
                offlineIntent.putExtra(Intent.EXTRA_TEXT, textToTranslate);
            }
        }

        startActivity(offlineIntent);
    }
}
