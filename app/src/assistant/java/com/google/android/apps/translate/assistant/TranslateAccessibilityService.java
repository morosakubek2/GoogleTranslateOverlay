package com.google.android.apps.translate.assistant;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class TranslateAccessibilityService extends AccessibilityService {
    private static final String TAG = "GOTr";
    private ClipboardManager clipboardManager;
    private Handler handler = new Handler(Looper.getMainLooper());
    
    private BroadcastReceiver assistantReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ACTIVATE_TRANSLATION_ASSISTANT".equals(intent.getAction())) {
                Log.d(TAG, "=== ASSISTANT ACTIVATED VIA BROADCAST ===");
                performTranslation();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        
        // Zarejestruj broadcast receiver
        IntentFilter filter = new IntentFilter("ACTIVATE_TRANSLATION_ASSISTANT");
        registerReceiver(assistantReceiver, filter);
        
        Log.d(TAG, "TranslateAccessibilityService created with broadcast receiver");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        info.notificationTimeout = 100;
        setServiceInfo(info);
        Log.d(TAG, "TranslateAccessibilityService connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Możemy monitorować zdarzenia, ale główna akcja jest triggerowana przez broadcast
        Log.d(TAG, "Accessibility event: " + event.getEventType());
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "TranslateAccessibilityService interrupted");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (assistantReceiver != null) {
            unregisterReceiver(assistantReceiver);
        }
        Log.d(TAG, "TranslateAccessibilityService destroyed");
    }

    private void performTranslation() {
        Log.d(TAG, "Performing translation sequence");
        
        // 1. Spróbuj skopiować tekst
        boolean copySuccess = performGlobalAction(GLOBAL_ACTION_COPY);
        Log.d(TAG, "Copy action result: " + copySuccess);
        
        if (copySuccess) {
            // 2. Poczekaj chwilę i sprawdź schowek
            handler.postDelayed(this::checkClipboardAndTranslate, 500);
        }
    }

    private void checkClipboardAndTranslate() {
        if (clipboardManager.hasPrimaryClip()) {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (text != null && !text.toString().trim().isEmpty()) {
                    String textToTranslate = text.toString().trim();
                    Log.d(TAG, "Text to translate: " + textToTranslate);
                    launchTranslation(textToTranslate);
                    return;
                }
            }
        }
        Log.d(TAG, "No text found in clipboard after copy attempt");
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
        }
    }
}
