package com.google.android.apps.translate.copydrop.gm3;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

public class TapToTranslateActivity extends Activity {

    private static final String OFFLINE_PACKAGE = "dev.davidv.translator";
    private static final String OFFLINE_PROCESS_ACTIVITY = ".ProcessTextActivity";

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
        offlineIntent.setComponent(new ComponentName(OFFLINE_PACKAGE, OFFLINE_PACKAGE + OFFLINE_PROCESS_ACTIVITY));

        String action = incomingIntent.getAction();
        if ("android.intent.action.PROCESS_TEXT".equals(action) || "android.intent.action.PROCESS_TEXT_READONLY".equals(action)) {
            String processText = incomingIntent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
            if (!TextUtils.isEmpty(processText)) {
                offlineIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, processText);
                offlineIntent.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, incomingIntent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false));
            }
        }

        startActivity(offlineIntent);
    }
}
