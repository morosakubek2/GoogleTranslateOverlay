package com.google.android.apps.translate.assistant;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
        setDisabledShowContext(0);
    }

    @Override
    public void launchVoiceAssistFromKeyguard() {
        Log.d(TAG, "Launching voice assist from keyguard");
        activateAccessibilityService();
        super.launchVoiceAssistFromKeyguard();
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        Log.d(TAG, "VoiceAssistantService shutdown");
    }

    private void activateAccessibilityService() {
        try {
            Intent intent = new Intent(this, TranslateAccessibilityService.class);
            intent.setAction("ACTIVATE_ASSISTANT");
            startService(intent);
            Log.d(TAG, "Accessibility service activation requested");
        } catch (Exception e) {
            Log.e(TAG, "Failed to activate accessibility service", e);
        }
    }
}
