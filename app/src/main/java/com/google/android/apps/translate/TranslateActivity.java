package com.google.android.apps.translate;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class TranslateActivity extends Activity {

    private static final String OFFLINE_PACKAGE = "dev.davidv.translator";
    private static final String OFFLINE_MAIN_ACTIVITY = ".MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GOTr", "TranslateActivity started");

        Intent incomingIntent = getIntent();
        if (incomingIntent != null) {
            redirectToOffline(incomingIntent);
        }
        finish();
    }

    private void redirectToOffline(Intent incomingIntent) {
        Log.d("GOTr", "Redirecting to offline translator");
        
        Intent offlineIntent = new Intent(incomingIntent);
        offlineIntent.setComponent(new ComponentName(OFFLINE_PACKAGE, OFFLINE_PACKAGE + OFFLINE_MAIN_ACTIVITY));

        String action = incomingIntent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            if ("text/plain".equals(incomingIntent.getType())) {
                String sharedText = incomingIntent.getStringExtra(Intent.EXTRA_TEXT);
                if (!TextUtils.isEmpty(sharedText)) {
                    offlineIntent.putExtra(Intent.EXTRA_TEXT, sharedText);
                    Log.d("GOTr", "Sending text: " + sharedText);
                }
            } else if (incomingIntent.getType() != null && incomingIntent.getType().startsWith("image/")) {
                Uri imageUri = incomingIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    offlineIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    Log.d("GOTr", "Sending image: " + imageUri);
                }
            }
        } else if ("android.intent.action.TRANSLATE".equals(action)) {
            String textToTranslate = incomingIntent.getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(textToTranslate)) {
                offlineIntent.putExtra(Intent.EXTRA_TEXT, textToTranslate);
                Log.d("GOTr", "Translating text: " + textToTranslate);
            }
        }

        startActivity(offlineIntent);
        Log.d("GOTr", "Successfully started offline translator");
    }
}
