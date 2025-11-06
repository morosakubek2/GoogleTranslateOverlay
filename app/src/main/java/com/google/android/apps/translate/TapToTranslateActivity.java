package com.google.android.apps.translate.copydrop.gm3;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class TapToTranslateActivity extends Activity {

    private static final String OFFLINE_PACKAGE = "dev.davidv.translator";
    private static final String OFFLINE_PROCESS_ACTIVITY = ".ProcessTextActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GOTr", "TapToTranslateActivity started");

        Intent incomingIntent = getIntent();
        if (incomingIntent != null) {
            processIncomingIntent(incomingIntent);
        }
        finish();
    }

    private void processIncomingIntent(Intent incomingIntent) {
        String action = incomingIntent.getAction();
        Log.d("GOTr", "Action: " + action);

        if (Intent.ACTION_PROCESS_TEXT.equals(action)) {
            String text = incomingIntent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
            if (TextUtils.isEmpty(text)) {
                Log.d("GOTr", "No text in PROCESS_TEXT, using clipboard fallback");
                // Jeśli nie ma tekstu, to może udało się skopiować do schowka?
                // Ale bez AccessibilityService nie przeczytamy schowka, więc pozostaje tylko uruchomić tłumacza bez tekstu
                launchOfflineTranslator(null);
            } else {
                Log.d("GOTr", "Processing text from PROCESS_TEXT: " + text);
                launchOfflineTranslator(text);
            }
        } else {
            Log.d("GOTr", "Unhandled intent action: " + action);
            redirectToOffline(incomingIntent);
        }
    }

    private void launchOfflineTranslator(String text) {
        Intent offlineIntent = new Intent();
        offlineIntent.setComponent(new ComponentName(OFFLINE_PACKAGE, OFFLINE_PACKAGE + OFFLINE_PROCESS_ACTIVITY));
        offlineIntent.setAction(Intent.ACTION_PROCESS_TEXT);
        offlineIntent.setType("text/plain");
        if (!TextUtils.isEmpty(text)) {
            offlineIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
        }
        offlineIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(offlineIntent);
            Log.d("GOTr", "Successfully started offline translator with text: " + text);
        } catch (Exception e) {
            Log.e("GOTr", "Failed to start offline translator", e);
        }
    }

    private void redirectToOffline(Intent incomingIntent) {
        Log.d("GOTr", "Redirecting to offline translator (PROCESS_TEXT)");
        
        Intent offlineIntent = new Intent(incomingIntent);
        offlineIntent.setComponent(new ComponentName(OFFLINE_PACKAGE, OFFLINE_PACKAGE + OFFLINE_PROCESS_ACTIVITY));

        String action = incomingIntent.getAction();
        if ("android.intent.action.PROCESS_TEXT".equals(action) || "android.intent.action.PROCESS_TEXT_READONLY".equals(action)) {
            String processText = incomingIntent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
            if (!TextUtils.isEmpty(processText)) {
                offlineIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, processText);
                offlineIntent.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, incomingIntent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false));
                Log.d("GOTr", "Processing text: " + processText);
            }
        }

        startActivity(offlineIntent);
        Log.d("GOTr", "Successfully started offline translator (PROCESS_TEXT)");
    }
}
