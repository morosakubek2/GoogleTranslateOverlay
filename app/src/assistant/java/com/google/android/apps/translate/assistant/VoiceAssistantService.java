package com.google.android.apps.translate.assistant;

import android.service.voice.VoiceInteractionService;
import android.util.Log;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class VoiceAssistantService extends VoiceInteractionService {
    private static final String TAG = "GOTr";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "VoiceAssistantService created");
    }

    @Override
    public void onReady() {
        super.onReady();
        Log.d(TAG, "VoiceAssistantService ready");
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        Log.d(TAG, "VoiceAssistantService shutdown");
    }

    // Wywołanie asystenta - aktywujemy AccessibilityService
    public void activateTranslationAssistant() {
        Log.d(TAG, "Activating translation assistant");
        
        // Wysyłamy broadcast do AccessibilityService
        Intent intent = new Intent("ACTIVATE_TRANSLATION_ASSISTANT");
        intent.setComponent(new ComponentName(
            getPackageName(),
            "com.google.android.apps.translate.assistant.TranslateAccessibilityService"
        ));
        sendBroadcast(intent);
        
        // Alternatywnie: bezpośrednie wywołanie przez system service
        triggerAccessibilityService();
    }

    private void triggerAccessibilityService() {
        try {
            // Ta metoda wymaga, żeby AccessibilityService miał publiczną metodę
            // W praktyce lepiej użyć BroadcastReceiver w AccessibilityService
            Log.d(TAG, "Attempting to trigger accessibility service");
        } catch (Exception e) {
            Log.e(TAG, "Failed to trigger accessibility service", e);
        }
    }
}
