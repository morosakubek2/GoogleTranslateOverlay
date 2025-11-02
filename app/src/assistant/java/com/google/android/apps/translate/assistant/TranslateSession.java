package com.google.android.apps.translate.assistant;

import android.service.voice.VoiceInteractionSession;
import android.content.Intent;
import android.os.Bundle;
import android.content.ComponentName;

public class TranslateSession extends VoiceInteractionSession {

    public TranslateSession(VoiceInteractionService service) {
        super(service);
    }

    @Override
    public void onHandleAssist(Bundle data, String[] hints, int[] offsets) {
        // Tylko jeśli tekst jest zaznaczony – ignorujemy wszystko inne (w tym mowę)
        String text = data.getString("android.intent.extra.TEXT");
        if (text != null && !text.trim().isEmpty()) {
            redirectToOffline(text);
        }
        finish();  // Zakończ natychmiast, bez GUI
    }

    private void redirectToOffline(String text) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("dev.davidv.translator", "dev.davidv.translator.ProcessTextActivity"));
        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }
}
