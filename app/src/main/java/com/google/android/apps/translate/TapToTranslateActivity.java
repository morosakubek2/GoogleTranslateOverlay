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
                Log.d("GOTr", "PROCESS_TEXT with empty text - launching translator for clipboard");
                launchOfflineTranslator(null);
            } else {
                Log.d("GOTr", "PROCESS_TEXT with text: " + text);
                launchOfflineTranslator(text);
            }
        } else {
            Log.d("GOTr", "Unhandled intent action: " + action);
            redirectToOffline(incomingIntent);
        }
    }

    private void launchOfflineTranslator(String text) {
        Intent offlineIntent = new Intent(Intent.ACTION_PROCESS_TEXT);
        offlineIntent.setComponent(new ComponentName(OFFLINE_PACKAGE, OFFLINE_PACKAGE + OFFLINE_PROCESS_ACTIVITY));
        offlineIntent.setType("text/plain");
        
        if (!TextUtils.isEmpty(text)) {
            offlineIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
        }
        
        offlineIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(offlineIntent);
        Log.d("GOTr", "Successfully started offline translator with text: " + (TextUtils.isEmpty(text) ? "FROM_CLIPBOARD" : text));
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
