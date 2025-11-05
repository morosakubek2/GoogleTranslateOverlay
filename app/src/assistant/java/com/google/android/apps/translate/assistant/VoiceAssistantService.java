package com.google.android.apps.translate.assistant;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.service.voice.VoiceInteractionService;
import android.util.Log;

public class VoiceAssistantService extends VoiceInteractionService {

    private static final String TAG = "GOTranslate";
    private TranslateAccessibilityService accessibilityService;
    private boolean isBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected to AccessibilityService");
            // accessibilityService = ((TranslateAccessibilityService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Disconnected from AccessibilityService");
            accessibilityService = null;
            isBound = false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "VoiceAssistantService created");
    }

    @Override
    public void onReady() {
        super.onReady();
        Log.d(TAG, "VoiceAssistantService ready");
        setDisabledShowContext(0);
        
        // Powiąż z Accessibility Service (jeśli jest włączony)
        // bindService(new Intent(this, TranslateAccessibilityService.class), connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "VoiceAssistantService start command");
        
        // Uruchom sesję Accessibility Service
        startAccessibilitySession();
        
        return START_NOT_STICKY; // Nie restartuj serwisu
    }

    private void startAccessibilitySession() {
        Log.d(TAG, "Starting accessibility session");
        // Wysyłamy broadcast do Accessibility Service
        Intent broadcastIntent = new Intent("START_ASSISTANT_SESSION");
        broadcastIntent.setPackage(getPackageName());
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        Log.d(TAG, "VoiceAssistantService shutdown");
        
        if (isBound) {
            // unbindService(connection);
            isBound = false;
        }
    }
}
