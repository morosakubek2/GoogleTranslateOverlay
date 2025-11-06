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
    private static final String TAG = "GOTr";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "SetupAssistantActivity started - action: " + getIntent().getAction());
        
        String action = getIntent().getAction();
        
        if (Intent.ACTION_MAIN.equals(action)) {
            Log.d(TAG, "Launched from launcher - checking services");
            checkAndSetupServices();
            finish();
            return;
        }
        
        if (Intent.ACTION_ASSIST.equals(action)) {
            Log.d(TAG, "Called with ACTION_ASSIST - system will handle via VoiceInteractionService");
            finish();
            return;
        }
        
        Log.d(TAG, "Unknown action, finishing");
        finish();
    }

    private void checkAndSetupServices() {
        boolean accessibilityEnabled = isAccessibilityEnabled();
        boolean voiceServiceEnabled = isVoiceServiceEnabled();
        
        Log.d(TAG, "Accessibility enabled: " + accessibilityEnabled);
        Log.d(TAG, "Voice service enabled: " + voiceServiceEnabled);
        
        if (!accessibilityEnabled) {
            Log.d(TAG, "Accessibility not enabled - prompting user");
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        } else if (!voiceServiceEnabled) {
            Log.d(TAG, "Voice service not enabled - opening voice settings");
            try {
                startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
            } catch (Exception e) {
                Log.e(TAG, "Failed to open voice settings", e);
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        } else {
            Log.d(TAG, "Both services are enabled - setup complete");
        }
    }

    private boolean isAccessibilityEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : enabledServices) {
            if (info.getId().contains("TranslateAccessibilityService")) {
                return true;
            }
        }
        return false;
    }

    private boolean isVoiceServiceEnabled() {
        try {
            String currentService = Settings.Secure.getString(getContentResolver(), 
                Settings.Secure.VOICE_INTERACTION_SERVICE);
            return currentService != null && currentService.contains("VoiceAssistantService");
        } catch (Exception e) {
            Log.e(TAG, "Error checking voice service", e);
            return false;
        }
    }
}
