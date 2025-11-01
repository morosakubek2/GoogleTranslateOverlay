package com.google.android.apps.translate.assistant;

import android.service.voice.VoiceInteractionSession;
import android.content.Intent;
import android.os.Bundle;

public class TranslateSession extends VoiceInteractionSession {
    public TranslateSession(VoiceInteractionService service) {
        super(service);
    }

    @Override
    public void onHandleIntent(Intent intent, Bundle extras) {
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (text != null) {
            redirectToOffline(text);
        }
        finish();
    }

    private void redirectToOffline(String text) {
        Intent i = new Intent();
        i.setClassName("dev.davidv.translator", "dev.davidv.translator.ProcessTextActivity");
        i.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(i);
    }
}
