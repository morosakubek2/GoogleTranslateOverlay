package com.google.android.apps.translate.assistant;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;
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

    @Override
    public boolean onSupportsAssist() {
        return true;
    }

    @Override
    public boolean onSupportsLaunchVoiceAssistFromKeyguard() {
        return false;
    }

    @Override
    public void launchVoiceAssistFromKeyguard() {
        Log.d("GOTr", "Voice assist from keyguard blocked");
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        Log.d("GOTr", "VoiceAssistantService shutdown");
    }
}
