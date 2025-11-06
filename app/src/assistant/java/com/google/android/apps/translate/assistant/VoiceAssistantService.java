package com.google.android.apps.translate.assistant;

import android.service.voice.VoiceInteractionService;
import android.util.Log;

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

    // Wywoływane gdy użytkownik aktywuje asystenta
    @Override
    public void onLaunchVoiceAssistFromKeyguard() {
        Log.d(TAG, "Voice assist launched from keyguard");
        activateTranslation();
        super.onLaunchVoiceAssistFromKeyguard();
    }

    private void activateTranslation() {
        Log.d(TAG, "Activating translation via accessibility service");
        // Tutaj możesz dodać kod do komunikacji z AccessibilityService
        // Na przykład przez broadcast lub bezpośrednie wywołanie
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        Log.d(TAG, "VoiceAssistantService shutdown");
    }
}
