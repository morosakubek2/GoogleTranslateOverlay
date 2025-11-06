package com.google.android.apps.translate.assistant;

import android.service.voice.VoiceInteractionService;
import android.util.Log;
import android.content.Intent;

public class VoiceAssistantService extends VoiceInteractionService {
    private static final String TAG = "GOTr";
    private static final String ACTION_ACTIVATE_ASSISTANT = "ACTIVATE_ASSISTANT";

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

    // Wywoływane gdy użytkownik aktywuje asystenta (np. z ekranu blokady)
    @Override
    public void onLaunchVoiceAssistFromKeyguard() {
        Log.d(TAG, "Voice assist launched from keyguard");
        activateTranslation();
        super.onLaunchVoiceAssistFromKeyguard();
    }

    // Możesz dodać inne metody aktywacji, np. z asystenta głosowego
    private void activateTranslation() {
        Log.d(TAG, "Activating translation via broadcast");
        // Wysyłamy broadcast do AccessibilityService
        Intent intent = new Intent(ACTION_ACTIVATE_ASSISTANT);
        intent.setPackage(getPackageName()); // Ograniczamy do naszej paczki
        sendBroadcast(intent);
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        Log.d(TAG, "VoiceAssistantService shutdown");
    }
}
