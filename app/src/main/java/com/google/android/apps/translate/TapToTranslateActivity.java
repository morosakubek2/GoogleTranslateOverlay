package com.google.android.apps.translate.assistant;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
        Log.d("GOTr", "onShow called with flags: " + showFlags);
    }

    @Override
    public void onHandleAssist(Bundle data, android.app.assist.AssistStructure structure, android.app.assist.AssistContent content) {
        Log.d("GOTr", "=== ASSIST START ===");
        
        Log.d("GOTr", "Triggering copy to clipboard");
        boolean copySuccess = performGlobalAction(GLOBAL_ACTION_COPY);
        
        if (copySuccess) {
            Log.d("GOTr", "Copy successful - launching translation from clipboard");
            launchTranslation();
        } else {
            Log.d("GOTr", "Copy failed - cannot proceed");
        }
        
        finish();
    }

    private void launchTranslation() {
        Intent processTextIntent = new Intent(Intent.ACTION_PROCESS_TEXT);
        processTextIntent.setType("text/plain");
        processTextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        processTextIntent.setComponent(new ComponentName(
            getContext().getPackageName(),
            "com.google.android.apps.translate.copydrop.gm3.TapToTranslateActivity"
        ));
        
        getContext().startActivity(processTextIntent);
        Log.d("GOTr", "Launched PROCESS_TEXT for clipboard translation");
    }

    @Override
    public void onHide() {
        super.onHide();
        Log.d("GOTr", "TranslateSession hidden");
    }
}
