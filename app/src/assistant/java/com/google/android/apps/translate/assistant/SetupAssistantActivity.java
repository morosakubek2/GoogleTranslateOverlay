package com.google.android.apps.translate.assistant;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

public class SetupAssistantActivity extends Activity {
    private static final String TAG = "GOTr";
    private ClipboardManager clipboardManager;
    private Handler handler = new Handler(Looper.getMainLooper());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "SetupAssistantActivity started - action: " + getIntent().getAction());
        
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String action = getIntent().getAction();
        
        if (Intent.ACTION_MAIN.equals(action)) {
            Log.d(TAG, "Launched from launcher - opening voice settings");
            openVoiceSettings();
        } else if (Intent.ACTION_ASSIST.equals(action)) {
            Log.d(TAG, "=== ASSISTANT TRIGGERED ===");
            performAutomaticTranslation();
        } else {
            Log.d(TAG, "Unknown action: " + action);
            finish();
        }
    }

    private void openVoiceSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Log.d(TAG, "Opened voice input settings");
        } catch (Exception e) {
            Log.e(TAG, "Failed to open voice settings", e);
        } finally {
            finish();
        }
    }

    private void performAutomaticTranslation() {
        Log.d(TAG, "Starting automatic translation process");
        
        // 1. Spróbuj automatycznie skopiować tekst
        attemptAutomaticCopy();
    }

    private void attemptAutomaticCopy() {
        Log.d(TAG, "Attempting automatic copy");
        
        // Wysyłamy broadcast do TranslateAccessibilityService
        Intent intent = new Intent("PERFORM_AUTO_COPY");
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
        Log.d(TAG, "Sent auto-copy broadcast to AccessibilityService");
        
        // Czekamy chwilę i sprawdzamy schowek
        handler.postDelayed(this::checkClipboardAndTranslate, 800);
    }

    private void checkClipboardAndTranslate() {
        if (clipboardManager.hasPrimaryClip()) {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (text != null && !text.toString().trim().isEmpty()) {
                    String textToTranslate = text.toString().trim();
                    Log.d(TAG, "Found text in clipboard: " + textToTranslate);
                    launchTranslation(textToTranslate);
                    return;
                }
            }
        }
        Log.d(TAG, "No text found in clipboard");
        finish();
    }

    private void launchTranslation(String text) {
        try {
            Intent processTextIntent = new Intent(Intent.ACTION_PROCESS_TEXT);
            processTextIntent.setType("text/plain");
            processTextIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
            processTextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            processTextIntent.setComponent(new ComponentName(
                getPackageName(),
                "com.google.android.apps.translate.copydrop.gm3.TapToTranslateActivity"
            ));
            
            startActivity(processTextIntent);
            Log.d(TAG, "Translation activity launched successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch translation activity", e);
        } finally {
            finish();
        }
    }
}
