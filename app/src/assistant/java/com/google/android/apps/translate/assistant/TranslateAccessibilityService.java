package com.google.android.apps.translate.assistant;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class TranslateAccessibilityService extends AccessibilityService {
    private static final String TAG = "GOTr";
    private ClipboardManager clipboardManager;
    private String originalClipboardContent;
    private boolean isAssistantTriggered = false;
    private long assistantTriggerTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        Log.d(TAG, "TranslateAccessibilityService created");
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
        Log.d(TAG, "TranslateAccessibilityService connected and configured");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Działa tylko gdy asystent został aktywowany
        if (!isAssistantTriggered) {
            return;
        }

        // Sprawdź czy minął maksymalny czas oczekiwania (2 sekundy)
        if (SystemClock.uptimeMillis() - assistantTriggerTime > 2000) {
            Log.d(TAG, "Assistant activation timeout - resetting");
            isAssistantTriggered = false;
            return;
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            handleAssistantActivation();
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "TranslateAccessibilityService interrupted");
    }

    // Metoda wywoływana przez Voice Service do aktywacji
    public void activateForTranslation() {
        Log.d(TAG, "Accessibility service activated for translation");
        isAssistantTriggered = true;
        assistantTriggerTime = SystemClock.uptimeMillis();
        saveOriginalClipboard();
    }

    private void handleAssistantActivation() {
        if (!isAssistantTriggered) return;

        Log.d(TAG, "Handling assistant activation - performing copy");
        boolean copySuccess = performGlobalAction(GLOBAL_ACTION_COPY);
        
        if (copySuccess) {
            SystemClock.sleep(300); // Czekaj na skopiowanie
            
            if (hasNewTextInClipboard()) {
                Log.d(TAG, "New text detected - launching translation");
                launchTranslation();
            } else {
                Log.d(TAG, "No new text in clipboard - skipping translation");
            }
        } else {
            Log.d(TAG, "Copy action failed");
        }
        
        // Reset bez względu na wynik
        isAssistantTriggered = false;
        restoreOriginalClipboard();
    }

    private void saveOriginalClipboard() {
        if (clipboardManager.hasPrimaryClip()) {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (text != null) {
                    originalClipboardContent = text.toString();
                    Log.d(TAG, "Saved original clipboard content");
                }
            }
        }
    }

    private boolean hasNewTextInClipboard() {
        if (!clipboardManager.hasPrimaryClip()) return false;

        ClipData clip = clipboardManager.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) return false;

        CharSequence newText = clip.getItemAt(0).getText();
        if (newText == null) return false;

        String newTextStr = newText.toString().trim();
        boolean hasNewText = !newTextStr.equals(originalClipboardContent) && 
                           !newTextStr.isEmpty() && 
                           newTextStr.length() > 1;
        
        Log.d(TAG, "New text in clipboard: " + hasNewText);
        return hasNewText;
    }

    private void restoreOriginalClipboard() {
        if (originalClipboardContent != null) {
            ClipData clip = ClipData.newPlainText("original", originalClipboardContent);
            clipboardManager.setPrimaryClip(clip);
            Log.d(TAG, "Restored original clipboard content");
        }
    }

    private void launchTranslation() {
        try {
            Intent processTextIntent = new Intent(Intent.ACTION_PROCESS_TEXT);
            processTextIntent.setType("text/plain");
            processTextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            processTextIntent.setComponent(new ComponentName(
                getPackageName(),
                "com.google.android.apps.translate.copydrop.gm3.TapToTranslateActivity"
            ));
            
            startActivity(processTextIntent);
            Log.d(TAG, "Translation activity launched");
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch translation activity", e);
        }
    }
}
