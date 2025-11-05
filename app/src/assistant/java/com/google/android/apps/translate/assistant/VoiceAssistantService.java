package com.google.android.apps.translate.assistant;

import android.service.voice.VoiceInteractionService;
import android.util.Log;

public class VoiceAssistantService extends VoiceInteractionService {

    private static final String TAG = "GOTranslate";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "VoiceAssistantService created");
    }

    @Override
    public void onReady() {
        super.onReady();
        Log.d(TAG, "VoiceAssistantService ready");
        setDisabledShowContext(0); // Ważne: pozwala na wyświetlanie kontekstu
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        Log.d(TAG, "VoiceAssistantService shutdown");
    }
}
