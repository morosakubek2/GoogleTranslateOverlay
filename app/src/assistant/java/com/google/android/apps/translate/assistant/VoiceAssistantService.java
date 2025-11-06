package com.google.android.apps.translate.assistant;

import android.service.voice.VoiceInteractionService;
import android.util.Log;
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

    // Gdy asystent jest wywołany, aktywujemy AccessibilityService
    @Override
    public void onLaunchVoiceAssistFromKeyguard() {
        Log.d(TAG, "Voice assist launched from keyguard - activating translation");
        activateTranslationAssistant();
    }

    private void activateTranslationAssistant() {
        Log.d(TAG, "Sending broadcast to activate accessibility service");
        
        // Wysyłamy broadcast do AccessibilityService
        Intent intent = new Intent("ACTIVATE_TRANSLATION_ASSISTANT");
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }
}
