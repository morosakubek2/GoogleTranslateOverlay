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
            // Sprawdź, czy to wywołanie z asystenta (bez konkretnego tekstu)
            if (isFromAssistant(incomingIntent)) {
                Log.d("GOTr", "Called from assistant - starting PROCESS_TEXT");
                // Uruchom PROCESS_TEXT, aby użytkownik mógł wybrać tekst
                startProcessText();
            } else {
                redirectToOffline(incomingIntent);
            }
        }
        finish();
    }

    private boolean isFromAssistant(Intent intent) {
        // Sprawdź, czy to wywołanie z asystenta (np. brak EXTRA_PROCESS_TEXT)
        return !intent.hasExtra(Intent.EXTRA_PROCESS_TEXT);
    }

    private void startProcessText() {
        Intent processTextIntent = new Intent(Intent.ACTION_PROCESS_TEXT);
        processTextIntent.setType("text/plain");
        processTextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Ustaw naszą aplikację jako handler, abyśmy mogli przechwycić wynik
        processTextIntent.setComponent(new ComponentName(
            getPackageName(),
            "com.google.android.apps.translate.copydrop.gm3.TapToTranslateActivity"
        ));

        startActivity(processTextIntent);
    }

    private void redirectToOffline(Intent incomingIntent) {
        Log.d("GOTr", "Redirecting to offline translator (PROCESS_TEXT)");
        
        Intent offlineIntent = new Intent(incomingIntent);
        offlineIntent.setComponent(new ComponentName(OFFLINE_PACKAGE, OFFLINE_PACKAGE + OFFLINE_PROCESS_ACTIVITY));

        String action = incomingIntent.getAction();
        if ("android.intent.action.PROCESS_TEXT".equals(action)) {
            String processText = incomingIntent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
            if (!TextUtils.isEmpty(processText)) {
                offlineIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, processText);
                Log.d("GOTr", "Processing text: " + processText);
            }
        }

        startActivity(offlineIntent);
        Log.d("GOTr", "Successfully started offline translator (PROCESS_TEXT)");
    }
}
