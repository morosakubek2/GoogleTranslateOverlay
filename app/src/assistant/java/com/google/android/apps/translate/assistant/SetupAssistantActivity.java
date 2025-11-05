package com.google.android.apps.translate.assistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class SetupAssistantActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d("GOTr", "SetupAssistantActivity started - action: " + getIntent().getAction());
        
        String action = getIntent().getAction();
        
        if (Intent.ACTION_MAIN.equals(action)) {
            Log.d("GOTr", "Launched from launcher - opening assistant settings");
            try {
                startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
            } catch (Exception e) {
                Log.e("GOTr", "Failed to open voice settings", e);
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
            finish();
            return;
        }
        
        if (Intent.ACTION_ASSIST.equals(action)) {
            Log.d("GOTr", "Called with ACTION_ASSIST - system will handle via VoiceInteractionService");
            finish();
            return;
        }
        
        Log.d("GOTr", "Unknown action, finishing");
        finish();
    }
}
