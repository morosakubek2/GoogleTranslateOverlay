package com.google.android.apps.translate.assistant;

import android.os.Bundle;
import android.service.voice.VoiceInteractionService;
import android.service.voice.VoiceInteractionSession;

public class VoiceAssistantService extends VoiceInteractionService {

    @Override
    public VoiceInteractionSession onNewSession(Bundle args) {
        return new TranslateSession(this);
    }
}
