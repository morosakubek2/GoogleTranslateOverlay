package com.google.android.apps.translate;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

public class TranslateActivity extends Activity {

    private static final String OFFLINE_PACKAGE = "dev.davidv.translator";
    private static final String OFFLINE_MAIN_ACTIVITY = ".MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent incomingIntent = getIntent();
        if (incomingIntent != null) {
            redirectToOffline(incomingIntent);
        }
        finish(); // Natychmiastowe zamknięcie dla oszczędności zasobów
    }

    private void redirectToOffline(Intent incomingIntent) {
        Intent offlineIntent = new Intent(incomingIntent);
        offlineIntent.setComponent(new ComponentName(OFFLINE_PACKAGE, OFFLINE_PACKAGE + OFFLINE_MAIN_ACTIVITY));

        String action = incomingIntent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            if ("text/plain".equals(incomingIntent.getType())) {
                String sharedText = incomingIntent.getStringExtra(Intent.EXTRA_TEXT);
                if (!TextUtils.isEmpty(sharedText)) {
                    offlineIntent.putExtra(Intent.EXTRA_TEXT, sharedText);
                }
            } else if (incomingIntent.getType() != null && incomingIntent.getType().startsWith("image/")) {
                Uri imageUri = incomingIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    offlineIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                }
            }
        } else if ("android.intent.action.TRANSLATE".equals(action)) {
            String textToTranslate = incomingIntent.getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(textToTranslate)) {
                offlineIntent.putExtra(Intent.EXTRA_TEXT, textToTranslate);
            }
        }

        startActivity(offlineIntent);
    }
}
