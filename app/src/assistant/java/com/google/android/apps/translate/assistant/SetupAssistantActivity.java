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
        
        Log.d("GOTr", "SetupAssistantActivity started - action: " + getIntent().getAction());
        
        String action = getIntent().getAction();
        
        if (Intent.ACTION_MAIN.equals(action)) {
            Log.d("GOTr", "Launched from launcher - checking accessibility");
            if (!isAccessibilityEnabled()) {
                Log.d("GOTr", "Accessibility not enabled - prompting user");
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } else {
                Log.d("GOTr", "Accessibility enabled - opening voice settings");
                try {
                    startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
                } catch (Exception e) {
                    Log.e("GOTr", "Failed to open voice settings", e);
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                }
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

    private boolean isAccessibilityEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : enabledServices) {
            if (info.getId().equals(getPackageName() + "/.assistant.TranslateAccessibilityService")) {
                return true;
            }
        }
        return false;
    }
}
