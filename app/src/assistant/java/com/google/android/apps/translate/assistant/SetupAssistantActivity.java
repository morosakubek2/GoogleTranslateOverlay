package com.google.android.apps.translate.assistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.accessibilityservice.AccessibilityServiceInfo;

import java.util.List;

public class SetupAssistantActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d("GOTr", "SetupAssistantActivity started");
        
        if (!isAccessibilityEnabled()) {
            Log.d("GOTr", "Opening accessibility settings");
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        } else if (!isAssistantEnabled()) {
            Log.d("GOTr", "Opening voice input settings");
            startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
        } else {
            Log.d("GOTr", "All services enabled");
        }
        
        finish();
    }

    private boolean isAccessibilityEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        );
        
        String serviceName = getPackageName() + "/.assistant.TranslateAccessibilityService";
        for (AccessibilityServiceInfo info : enabledServices) {
            if (info.getId().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAssistantEnabled() {
        String currentAssistant = Settings.Secure.getString(
            getContentResolver(),
            Settings.Secure.VOICE_INTERACTION_SERVICE
        );
        
        String ourService = getPackageName() + "/.assistant.VoiceAssistantService";
        return ourService.equals(currentAssistant);
    }
}
