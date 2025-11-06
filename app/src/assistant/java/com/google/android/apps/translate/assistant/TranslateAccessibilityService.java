package com.google.android.apps.translate.assistant;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class TranslateAccessibilityService extends AccessibilityService {

    private static TranslateAccessibilityService instance;
    private ClipboardManager clipboardManager;
    private String lastClipboardContent;
    private long lastClipboardTime;
    private boolean shouldProcessNextClipboard;
    private Handler handler;
    
    private static final long CLIPBOARD_TIMEOUT_MS = 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        handler = new Handler(Looper.getMainLooper());
        
        saveCurrentClipboard();
        
        clipboardManager.addPrimaryClipChangedListener(() -> {
            if (shouldProcessNextClipboard) {
                handleClipboardChange();
            }
        });
        
        Log.d("GOTr", "TranslateAccessibilityService created");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Używane tylko dla utrzymania serwisu
    }

    @Override
    public void onInterrupt() {
        Log.d("GOTr", "TranslateAccessibilityService interrupted");
    }

    public static void triggerTranslation() {
        if (instance != null) {
            instance.performTranslationSequence();
        } else {
            Log.d("GOTr", "AccessibilityService not available");
        }
    }

    private void performTranslationSequence() {
        Log.d("GOTr", "Starting translation sequence");
        
        saveCurrentClipboard();
        shouldProcessNextClipboard = true;
        
        handler.postDelayed(() -> {
            shouldProcessNextClipboard = false;
            Log.d("GOTr", "Translation sequence timeout");
        }, CLIPBOARD_TIMEOUT_MS);
        
        boolean copySuccess = performGlobalAction(GLOBAL_ACTION_COPY);
        Log.d("GOTr", "Copy action triggered: " + copySuccess);
    }

    private void saveCurrentClipboard() {
        if (clipboardManager.hasPrimaryClip()) {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (text != null) {
                    lastClipboardContent = text.toString();
                    lastClipboardTime = System.currentTimeMillis();
                }
            }
        }
    }

    private void handleClipboardChange() {
        shouldProcessNextClipboard = false;
        handler.removeCallbacksAndMessages(null);
        
        if (!clipboardManager.hasPrimaryClip()) {
            Log.d("GOTr", "No clipboard content");
            return;
        }

        ClipData clip = clipboardManager.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            Log.d("GOTr", "Empty clipboard");
            return;
        }

        CharSequence newText = clip.getItemAt(0).getText();
        if (newText == null) {
            Log.d("GOTr", "Null clipboard text");
            return;
        }

        String newTextStr = newText.toString().trim();
        long currentTime = System.currentTimeMillis();
        
        if (newTextStr.isEmpty() || newTextStr.length() < 2) {
            Log.d("GOTr", "Text too short");
            restoreClipboard();
            return;
        }
        
        if (newTextStr.equals(lastClipboardContent)) {
            Log.d("GOTr", "Same text as before");
            return;
        }
        
        if (currentTime - lastClipboardTime > CLIPBOARD_TIMEOUT_MS) {
            Log.d("GOTr", "Clipboard change too old");
            restoreClipboard();
            return;
        }
        
        Log.d("GOTr", "Valid new text detected, launching translation");
        launchTranslation();
    }

    private void restoreClipboard() {
        if (lastClipboardContent != null) {
            ClipData clip = ClipData.newPlainText("restored", lastClipboardContent);
            clipboardManager.setPrimaryClip(clip);
            Log.d("GOTr", "Clipboard restored");
        }
    }

    private void launchTranslation() {
        Intent intent = new Intent(Intent.ACTION_PROCESS_TEXT);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(
            getPackageName(),
            "com.google.android.apps.translate.copydrop.gm3.TapToTranslateActivity"
        ));
        
        startActivity(intent);
        Log.d("GOTr", "Translation launched");
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
        Log.d("GOTr", "TranslateAccessibilityService destroyed");
    }
}
