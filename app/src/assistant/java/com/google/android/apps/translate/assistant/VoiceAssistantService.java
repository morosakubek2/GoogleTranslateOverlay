package com.google.android.apps.translate.assistant;

import android.content.ComponentName;
import android.service.voice.VoiceInteractionService;

public class VoiceAssistantService extends VoiceInteractionService {

    @Override
    public void onReady() {
        super.onReady();
        // Wskazujemy, że sesja jest obsługiwana przez VoiceInteractionSessionService
    }

    @Override
    public ComponentName getSessionComponentName() {
        return new ComponentName(this, VoiceInteractionSessionService.class);
    }
}
