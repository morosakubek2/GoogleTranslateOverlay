package com.google.android.apps.translate.assistant;

import android.service.voice.VoiceInteractionSession;
import android.service.voice.VoiceInteractionSession.AssistState;
import android.content.Intent;
import android.content.ComponentName;
import android.text.TextUtils;

public class TranslateSession extends VoiceInteractionSession {

    public TranslateSession(VoiceInteractionService service) {
        super(service);
    }

    @Override
    public void onHandleAssist(AssistState state) {
        super.onHandleAssist(state);
        
        // Pobierz zaznaczony tekst z AssistState
        String text = state.getSelectedText();
        if (!TextUtils.isEmpty(text)) {
            redirectToOffline(text);
        } else {
            finish(); // Zaprzestaj, jeśli nie ma zaznaczonego tekstu
        }
    }

    private void redirectToOffline(String text) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("dev.davidv.translator", "dev.davidv.translator.ProcessTextActivity"));
        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
        finish();
    }
}
