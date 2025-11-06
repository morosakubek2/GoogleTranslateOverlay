package com.google.android.apps.translate.assistant;

import android.service.voice.VoiceInteractionSession;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class TranslateSession extends VoiceInteractionSession {
    private static final String TAG = "GOTr";

    public TranslateSession(Context context) {
        super(context);
        Log.d(TAG, "TranslateSession created");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "TranslateSession onCreate");
    }

    @Override
    public void onShow(Bundle args, int showFlags) {
        super.onShow(args, showFlags);
        Log.d(TAG, "TranslateSession shown - activating assistant");
        
        // Aktywuj tryb asystenta w AccessibilityService
        activateAssistantMode();
    }

    private void activateAssistantMode() {
        try {
            // Próbujemy aktywować AccessibilityService przez VoiceAssistantService
            VoiceAssistantService service = (VoiceAssistantService) getContext();
            service.activateTranslationAssistant();
        } catch (Exception e) {
            Log.e(TAG, "Failed to activate assistant mode", e);
        }
        
        // Zamykamy sesję - główna praca będzie w AccessibilityService
        hide();
    }

    @Override
    public void onHide() {
        super.onHide();
        Log.d(TAG, "TranslateSession hidden");
    }
}
