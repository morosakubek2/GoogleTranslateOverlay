package com.google.android.apps.translate.assistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class SetupAssistantActivity extends Activity {
    private static final String TAG = "GOTr";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "SetupAssistantActivity started");
        
        // Zawsze otwieraj ustawienia asystenta głosowego
        try {
            Intent voiceSettings = new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS);
            startActivity(voiceSettings);
            Log.d(TAG, "Opened voice input settings");
        } catch (Exception e) {
            Log.e(TAG, "Failed to open voice settings, opening general settings", e);
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
        
        finish();
    }
}
