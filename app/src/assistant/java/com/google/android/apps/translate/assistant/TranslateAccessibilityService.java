package com.google.android.apps.translate.assistant;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.List;

public class TranslateAccessibilityService extends AccessibilityService {

    private static final long SESSION_TIMEOUT_MS = 5000;
    private static final long DEBOUNCE_DELAY_MS = 300;
    private static final long RESUME_DELAY_MS = 800;

    private static TranslateAccessibilityService instance;

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isAssistantSession = false;
    private long lastEventTime = 0;
    private Runnable timeoutRunnable = this::endSession;
    private Runnable checkSelectionRunnable = this::checkCurrentSelection;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d("GOTr", "TranslateAccessibilityService connected – applying config");

        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
                | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        config.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
                | AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        config.notificationTimeout = 100;

        setServiceInfo(config);
    }

    public static boolean startSession() {
        if (instance != null) {
            instance.startAssistantSession();
            return true;
        }
        Log.w("GOTr", "AccessibilityService not running!");
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "START_SESSION".equals(intent.getAction())) {
            Log.d("GOTr", "Received start session command");
            startAssistantSession();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isAssistantSession) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEventTime < DEBOUNCE_DELAY_MS) return;
        lastEventTime = currentTime;

        Log.d("GOTr", "Accessibility event: " + event.getEventType() + ", package: " + event.getPackageName());

        handler.removeCallbacks(timeoutRunnable);
        handler.postDelayed(timeoutRunnable, SESSION_TIMEOUT_MS);

        int type = event.getEventType();
        if (type == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            Log.d("GOTr", "TEXT SELECTION CHANGED: start=" + event.getFromIndex() + ", end=" + event.getToIndex());
            processTextSelection(event);
        } else if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
                   type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            Log.d("GOTr", "Window changed – checking selection");
            checkCurrentSelection();
        }
    }

    @Override
    public void onInterrupt() {
        Log.d("GOTr", "TranslateAccessibilityService interrupted");
        endSession();
    }

    private void startAssistantSession() {
        Log.d("GOTr", "Starting assistant session");
        isAssistantSession = true;
        lastEventTime = System.currentTimeMillis();
        
        handler.postDelayed(checkSelectionRunnable, RESUME_DELAY_MS);
        
        handler.removeCallbacks(timeoutRunnable);
        handler.postDelayed(timeoutRunnable, SESSION_TIMEOUT_MS);
    }

    private void endSession() {
        Log.d("GOTr", "Ending assistant session");
        isAssistantSession = false;
        handler.removeCallbacks(timeoutRunnable);
        handler.removeCallbacks(checkSelectionRunnable);
    }

    private void checkCurrentSelection() {
        List<AccessibilityWindowInfo> windows = getWindows();
        if (windows == null || windows.isEmpty()) {
            Log.d("GOTr", "No windows found");
            return;
        }

        Log.d("GOTr", "Found " + windows.size() + " windows");

        for (AccessibilityWindowInfo window : windows) {
            AccessibilityNodeInfo root = window.getRoot();
            if (root == null) continue;

            String selectedText = findSelectedTextInNode(root);
            root.recycle();

            if (!TextUtils.isEmpty(selectedText)) {
                Log.d("GOTr", "Found current selected text: " + selectedText);
                redirectToTranslateActivity(selectedText);
                endSession();
                return;
            }
        }

        Log.d("GOTr", "No current selection found in any window");
    }

    private void processTextSelection(AccessibilityEvent event) {
        String selectedText = findSelectedText(event);
        if (!TextUtils.isEmpty(selectedText)) {
            Log.d("GOTr", "Found selected text: " + selectedText);
            redirectToTranslateActivity(selectedText);
            endSession();
        }
    }

    private String findSelectedText(AccessibilityEvent event) {
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return null;
        String result = findSelectedTextInNode(source);
        source.recycle();
        return result;
    }

    private String findSelectedTextInNode(AccessibilityNodeInfo node) {
        if (node == null) return null;

        CharSequence text = node.getText();
        if (text != null && text.length() > 1) {
            int start = node.getTextSelectionStart();
            int end = node.getTextSelectionEnd();
            if (start >= 0 && end > start && end <= text.length()) {
                String sel = text.subSequence(start, end).toString().trim();
                if (!TextUtils.isEmpty(sel) && sel.length() > 1) {
                    Log.d("GOTr", "Selection via TextSelection: " + sel);
                    return sel;
                }
            }
        }

        if (node.isSelected() && text != null && text.length() > 1) {
            String sel = text.toString().trim();
            if (!TextUtils.isEmpty(sel)) {
                Log.d("GOTr", "Selection via isSelected(): " + sel);
                return sel;
            }
        }

        int childCount = node.getChildCount();
        for (int i = 0; i < childCount && i < 200; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                String result = findSelectedTextInNode(child);
                child.recycle();
                if (result != null) return result;
            }
        }
        return null;
    }

    private void redirectToTranslateActivity(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setComponent(new ComponentName(
            getPackageName(),
            "com.google.android.apps.translate.TranslateActivity"
        ));
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        startActivity(intent);
        Log.d("GOTr", "Successfully redirected to TranslateActivity");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d("GOTr", "TranslateAccessibilityService destroyed");
        handler.removeCallbacks(timeoutRunnable);
        handler.removeCallbacks(checkSelectionRunnable);
    }
}
