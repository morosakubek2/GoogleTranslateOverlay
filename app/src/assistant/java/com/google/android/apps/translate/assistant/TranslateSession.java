package com.google.android.apps.translate.assistant;

import android.content.Context;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.util.Log;

public class TranslateSession extends VoiceInteractionSession {

    public TranslateSession(Context context) {
        super(context);
        Log.d("GOTr", "TranslateSession created");
    }

    @Override
    public void onShow(Bundle args, int showFlags) {
        super.onShow(args, showFlags);
        Log.d("GOTr", "TranslateSession onShow");
        
        TranslateAccessibilityService.triggerTranslation();
        
        finish();
    }

    @Override
    public void onHide() {
        super.onHide();
        Log.d("GOTr", "TranslateSession hidden");
    }
}
