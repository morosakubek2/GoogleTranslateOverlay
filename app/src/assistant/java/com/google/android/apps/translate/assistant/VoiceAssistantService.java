package com.google.android.apps.translate.assistant;

import android.service.voice.VoiceInteractionService;
import android.service.voice.VoiceInteractionSession;
import android.os.Bundle;

public class VoiceAssistantService extends VoiceInteractionService {
    @Override
    public VoiceInteractionSession onCreateSession(String sessionId, Bundle args) {
        return new TranslateSession(this);
    }
}
