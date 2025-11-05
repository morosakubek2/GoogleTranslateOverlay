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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("GOTr", "VoiceAssistantService start command");
        return START_NOT_STICKY;
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        Log.d("GOTr", "VoiceAssistantService shutdown");
    }
}
