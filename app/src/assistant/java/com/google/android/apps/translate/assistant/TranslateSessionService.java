package com.google.android.apps.translate.assistant;

import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.service.voice.VoiceInteractionSessionService;
import android.util.Log;

public class TranslateSessionService extends VoiceInteractionSessionService {

    @Override
    public VoiceInteractionSession onNewSession(Bundle args) {
        Log.d("GOTranslate", "Creating new TranslateSession");
        return new TranslateSession(this);
    }
}
