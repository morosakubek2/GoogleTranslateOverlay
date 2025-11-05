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
        
        Log.d("GOTranslate", "SetupAssistantActivity started - action: " + getIntent().getAction());
        Log.d("GOTranslate", "SetupAssistantActivity - flags: " + getIntent().getFlags());
        Log.d("GOTranslate", "SetupAssistantActivity - categories: " + getIntent().getCategories());
        
        String action = getIntent().getAction();
        
        if (Intent.ACTION_MAIN.equals(action)) {
            Log.d("GOTranslate", "Launched from launcher - opening assistant settings");
            try {
                startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
            } catch (Exception e) {
                Log.e("GOTranslate", "Failed to open voice settings", e);
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
            finish();
            return;
        }
        
        if (Intent.ACTION_ASSIST.equals(action)) {
            Log.d("GOTranslate", "Called with ACTION_ASSIST - system will handle via VoiceInteractionService");
            finish();
            return;
        }
        
        Log.d("GOTranslate", "Unknown action, finishing");
        finish();
    }
}
