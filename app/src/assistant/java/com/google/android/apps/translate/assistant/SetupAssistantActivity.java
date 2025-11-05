package com.google.android.apps.translate.assistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class SetupAssistantActivity extends Activity {
    
    private static final String TAG = "SetupAssistantActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "onCreate - action: " + getIntent().getAction());
        
        String action = getIntent().getAction();
        
        // Jeśli wywołano z launchera - otwórz ustawienia asystenta
        if (Intent.ACTION_MAIN.equals(action)) {
            Log.d(TAG, "Launched from launcher - opening assistant settings");
            try {
                startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
            } catch (Exception e) {
                Log.e(TAG, "Failed to open voice settings", e);
                // Fallback do głównych ustawień
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
            finish();
            return;
        }
        
        // Jeśli wywołano jako ASSIST - to znaczy że użytkownik wywołał asystenta
        // W tym przypadku Android sam wywołuje VoiceInteractionService
        if (Intent.ACTION_ASSIST.equals(action)) {
            Log.d(TAG, "Called with ACTION_ASSIST - system will handle via VoiceInteractionService");
            // Nie robimy nic - system sam przekieruje do VoiceInteractionService
            finish();
            return;
        }
        
        // Inne przypadki - zamknij
        Log.d(TAG, "Unknown action, finishing");
        finish();
    }
}
