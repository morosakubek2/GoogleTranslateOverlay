package com.google.android.apps.translate.assistant;

import android.content.Intent;
import android.service.voice.VoiceInteractionService;
import android.util.Log;

public class VoiceAssistantService extends VoiceInteractionService {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("GOTr", "VoiceAssistantService created");
    }

    @Override
    public void onReady() {
        super.onReady();
        Log.d("GOTr", "VoiceAssistantService ready");
        setDisabledShowContext(0);
    }

    // ZMIANA: Obsłuż launch z keyguard – start sesję accessibility bezpośrednio
    @Override
    public void onLaunchVoiceAssistFromKeyguard() {
        Log.d("GOTr", "Launched from keyguard – starting accessibility session");
        Intent serviceIntent = new Intent(this, TranslateAccessibilityService.class);
        serviceIntent.setAction("START_SESSION");
        startService(serviceIntent);
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        Log.d("GOTr", "VoiceAssistantService shutdown");
    }
}
