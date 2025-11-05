package com.google.android.apps.translate.assistant;

import android.content.Intent;
import android.service.voice.VoiceInteractionService;

public class VoiceAssistantService extends VoiceInteractionService {

    @Override
    public void onReady() {
        super.onReady();
        // Serwis jest gotowy - Android automatycznie używa sessionService z XML
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
