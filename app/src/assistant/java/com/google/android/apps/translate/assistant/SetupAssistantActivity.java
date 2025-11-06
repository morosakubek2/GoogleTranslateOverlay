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
        
        Log.d("GOTr", "SetupAssistantActivity started");
        
        Log.d("GOTr", "Opening voice input settings to set our app as assistant");
        try {
            startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
        } catch (Exception e) {
            Log.e("GOTr", "Failed to open voice input settings", e);
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
        
        finish();
    }
}
