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
            redirectToOffline(incomingIntent);
        }
        finish();
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
