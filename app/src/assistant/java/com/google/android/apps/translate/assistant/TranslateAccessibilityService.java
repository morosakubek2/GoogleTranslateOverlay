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
import android.view.accessibility.AccessibilityNodeInfo;

public class TranslateAccessibilityService extends AccessibilityService {
    private static final String TAG = "GOTr";
    private ClipboardManager clipboardManager;
    private String originalClipboardContent;
    private long lastAssistantActivationTime = 0;
    private boolean isAssistantActive = false;

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
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | 
                         AccessibilityEvent.TYPE_VIEW_FOCUSED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.notificationTimeout = 100;
        setServiceInfo(info);
        Log.d(TAG, "TranslateAccessibilityService connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isAssistantActive) {
            return;
        }

        if (SystemClock.uptimeMillis() - lastAssistantActivationTime > 2000) {
            isAssistantActive = false;
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

    public void activateAssistant() {
        if (!isAssistantActive) {
            lastAssistantActivationTime = SystemClock.uptimeMillis();
            isAssistantActive = true;
            saveOriginalClipboard();
            Log.d(TAG, "Assistant activated - ready to capture text");
        }
    }

    private void handleAssistantActivation() {
        if (!isAssistantActive) return;

        boolean copySuccess = performGlobalAction(GLOBAL_ACTION_COPY);
        Log.d(TAG, "Copy action performed: " + copySuccess);

        if (copySuccess) {
            SystemClock.sleep(300);
            
            if (hasNewTextInClipboard()) {
                Log.d(TAG, "New text detected - launching translation");
                launchTranslation();
            } else {
                Log.d(TAG, "No new text in clipboard - skipping translation");
                restoreOriginalClipboard();
            }
        }
        
        isAssistantActive = false;
    }

    private void saveOriginalClipboard() {
        if (clipboardManager.hasPrimaryClip()) {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (text != null) {
                    originalClipboardContent = text.toString();
                    Log.d(TAG, "Saved original clipboard: " + originalClipboardContent);
                }
            }
        }
    }

    private boolean hasNewTextInClipboard() {
        if (!clipboardManager.hasPrimaryClip()) {
            return false;
        }

        ClipData clip = clipboardManager.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            return false;
        }

        CharSequence newText = clip.getItemAt(0).getText();
        if (newText == null) {
            return false;
        }

        String newTextStr = newText.toString().trim();
        boolean hasNewText = !newTextStr.equals(originalClipboardContent) && 
                           !newTextStr.isEmpty() && 
                           newTextStr.length() > 1;
        
        Log.d(TAG, "Clipboard check - original: '" + originalClipboardContent + "', new: '" + newTextStr + "', hasNewText: " + hasNewText);
        
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
        Intent processTextIntent = new Intent(Intent.ACTION_PROCESS_TEXT);
        processTextIntent.setType("text/plain");
        processTextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        processTextIntent.setComponent(new ComponentName(
            getPackageName(),
            "com.google.android.apps.translate.copydrop.gm3.TapToTranslateActivity"
        ));
        
        startActivity(processTextIntent);
        Log.d(TAG, "Launched translation activity");
    }
}
