package com.google.android.apps.translate.assistant;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TranslateAccessibilityService extends AccessibilityService {
    private static final String TAG = "GOTr";
    private ClipboardManager clipboardManager;
    private String originalClipboardContent;
    private boolean isAssistantActive = false;
    private Handler handler = new Handler(Looper.getMainLooper());

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
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED | 
                         AccessibilityEvent.TYPE_VIEW_FOCUSED |
                         AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.notificationTimeout = 100;
        setServiceInfo(info);
        Log.d(TAG, "TranslateAccessibilityService connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isAssistantActive) return;

        Log.d(TAG, "Accessibility event: " + event.getEventType());
        
        // Gdy asystent jest aktywny, próbujemy znaleźć i skopiować tekst
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            
            handler.postDelayed(this::attemptCopyText, 100);
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "TranslateAccessibilityService interrupted");
    }

    public void activateAssistantMode() {
        Log.d(TAG, "Assistant mode activated");
        isAssistantActive = true;
        saveOriginalClipboard();
        
        // Automatyczna próba kopiowania po aktywacji
        handler.postDelayed(this::attemptCopyText, 200);
        
        // Timeout po 3 sekundach
        handler.postDelayed(() -> {
            if (isAssistantActive) {
                Log.d(TAG, "Assistant mode timeout");
                isAssistantActive = false;
                restoreOriginalClipboard();
            }
        }, 3000);
    }

    private void attemptCopyText() {
        if (!isAssistantActive) return;

        Log.d(TAG, "Attempting to copy text from focused view");
        
        // Pobierz root node i znajdź zaznaczony tekst
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.d(TAG, "No root node available");
            return;
        }

        // Szukaj zaznaczonego tekstu
        AccessibilityNodeInfo selectedNode = findSelectedText(rootNode);
        if (selectedNode != null) {
            Log.d(TAG, "Found selected text node - attempting copy");
            
            // Wykonaj akcję kopiowania
            boolean copySuccess = selectedNode.performAction(AccessibilityNodeInfo.ACTION_COPY);
            Log.d(TAG, "Copy action result: " + copySuccess);
            
            if (copySuccess) {
                handler.postDelayed(this::checkClipboardAndTranslate, 300);
            }
            
            selectedNode.recycle();
        } else {
            Log.d(TAG, "No selected text found");
        }
        
        rootNode.recycle();
    }

    private AccessibilityNodeInfo findSelectedText(AccessibilityNodeInfo root) {
        // Szukaj węzła z zaznaczonym tekstem
        if (root.isFocused() && root.isSelected() && root.getText() != null) {
            return root;
        }

        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child != null) {
                if (child.isFocused() && child.isSelected() && child.getText() != null) {
                    return child;
                }
                
                AccessibilityNodeInfo result = findSelectedText(child);
                if (result != null) {
                    return result;
                }
                child.recycle();
            }
        }
        return null;
    }

    private void checkClipboardAndTranslate() {
        if (!isAssistantActive) return;

        if (hasNewTextInClipboard()) {
            Log.d(TAG, "New text detected in clipboard - launching translation");
            launchTranslation();
        } else {
            Log.d(TAG, "No new text in clipboard");
        }
        
        isAssistantActive = false;
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
        
        Log.d(TAG, "Clipboard has new text: " + hasNewText + " - '" + newTextStr + "'");
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
