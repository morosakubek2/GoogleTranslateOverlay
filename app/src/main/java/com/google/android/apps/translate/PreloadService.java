package com.google.android.apps.translate;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PreloadService extends Service {
    private static final String TAG = "PreloadService";
    private static final String TARGET_PACKAGE = "dev.davidv.translator";
    private static final String TARGET_ACTIVITY = ".ProcessTextActivity";

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preloadTranslator();
        return START_STICKY; // ← przeżyje zabicie
    }

    private void preloadTranslator() {
        try {
            Intent preloadIntent = new Intent();
            preloadIntent.setComponent(new ComponentName(TARGET_PACKAGE, TARGET_PACKAGE + TARGET_ACTIVITY));
            preloadIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(preloadIntent);
            Log.d(TAG, "Translator preloaded in background");
        } catch (Exception e) {
            Log.e(TAG, "Failed to preload", e);
        }
    }
}
