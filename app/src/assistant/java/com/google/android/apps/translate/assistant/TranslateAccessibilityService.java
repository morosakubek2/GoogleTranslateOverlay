package com.google.android.apps.translate.assistant;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TranslateAccessibilityService extends AccessibilityService {

    private ClipboardManager clipboardManager;
    private String originalClipboardContent;

    @Override
    public void onCreate() {
        super.onCreate();
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("GOTr", "TranslateAccessibilityService connected");
        
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence packageName = event.getPackageName();
            CharSequence className = event.getClassName();
            Log.d("GOTr", "Window state changed: " + packageName + "/" + className);
            
            // Sprawdź, czy to okno asystenta (może różnić się w zależności od urządzenia)
            if (isAssistantWindow(packageName, className)) {
                Log.d("GOTr", "Assistant window detected - triggering copy");
                saveOriginalClipboard();
                boolean copySuccess = performGlobalAction(GLOBAL_ACTION_COPY);
                if (copySuccess) {
                    // Poczekaj chwilę na skopiowanie
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    if (hasNewTextInClipboard()) {
                        Log.d("GOTr", "New text detected in clipboard - launching translation");
                        launchTranslation();
                    } else {
                        Log.d("GOTr", "No new text in clipboard - nothing to translate");
                        restoreOriginalClipboard();
                    }
                } else {
                    Log.d("GOTr", "Copy action failed");
                }
            }
        }
    }

    private boolean isAssistantWindow(CharSequence packageName, CharSequence className) {
        // Dopasuj do swojego asystenta - może to być package systemowy lub inny
        // Na przykład: "com.google.android.googlequicksearchbox" i klasa "com.google.android.apps.gsa.staticplugins.opa.OpaActivity"
        // To może wymagać dostosowania dla różnych urządzeń
        return "com.google.android.googlequicksearchbox".equals(packageName) && 
               "com.google.android.apps.gsa.staticplugins.opa.OpaActivity".equals(className);
    }

    private void saveOriginalClipboard() {
        if (clipboardManager.hasPrimaryClip()) {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (text != null) {
                    originalClipboardContent = text.toString();
                    Log.d("GOTr", "Saved original clipboard: " + originalClipboardContent);
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
        
        // Sprawdź czy nowy tekst jest inny niż oryginalny i nie jest pusty
        boolean hasNewText = !newTextStr.equals(originalClipboardContent) && 
                           !newTextStr.isEmpty() && 
                           newTextStr.length() > 1;
        
        Log.d("GOTr", "Clipboard check - original: '" + originalClipboardContent + "', new: '" + newTextStr + "', hasNewText: " + hasNewText);
        
        return hasNewText;
    }

    private void restoreOriginalClipboard() {
        if (originalClipboardContent != null) {
            ClipData clip = ClipData.newPlainText("original", originalClipboardContent);
            clipboardManager.setPrimaryClip(clip);
            Log.d("GOTr", "Restored original clipboard content");
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
        Log.d("GOTr", "Launched translation");
    }

    @Override
    public void onInterrupt() {
        Log.d("GOTr", "TranslateAccessibilityService interrupted");
    }
}
