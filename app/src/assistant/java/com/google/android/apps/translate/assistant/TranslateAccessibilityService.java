package com.google.android.apps.translate.assistant;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TranslateAccessibilityService extends AccessibilityService {
    private static final String TAG = "GOTr";
    
    private BroadcastReceiver copyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("PERFORM_AUTO_COPY".equals(intent.getAction())) {
                Log.d(TAG, "Received auto-copy command");
                performAutoCopy();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Zarejestruj broadcast receiver
        IntentFilter filter = new IntentFilter("PERFORM_AUTO_COPY");
        registerReceiver(copyReceiver, filter);
        
        Log.d(TAG, "TranslateAccessibilityService created");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | 
                         AccessibilityEvent.TYPE_VIEW_FOCUSED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        info.notificationTimeout = 100;
        setServiceInfo(info);
        Log.d(TAG, "TranslateAccessibilityService connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "Accessibility event: " + event.getEventType());
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "TranslateAccessibilityService interrupted");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (copyReceiver != null) {
            unregisterReceiver(copyReceiver);
        }
        Log.d(TAG, "TranslateAccessibilityService destroyed");
    }

    private void performAutoCopy() {
        Log.d(TAG, "Performing automatic copy");
        
        try {
            // Pobierz root node
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) {
                Log.d(TAG, "No root node available");
                return;
            }

            // Spróbuj znaleźć i skopiować zaznaczony tekst
            boolean copySuccess = findAndCopySelectedText(rootNode);
            Log.d(TAG, "Copy result: " + copySuccess);
            
            rootNode.recycle();
        } catch (Exception e) {
            Log.e(TAG, "Error in auto copy", e);
        }
    }

    private boolean findAndCopySelectedText(AccessibilityNodeInfo root) {
        // Szukaj węzła z zaznaczonym tekstem
        AccessibilityNodeInfo selectedNode = findSelectedNode(root);
        if (selectedNode != null) {
            Log.d(TAG, "Found selected node, attempting copy");
            boolean result = selectedNode.performAction(AccessibilityNodeInfo.ACTION_COPY);
            selectedNode.recycle();
            return result;
        }
        
        Log.d(TAG, "No selected text found, trying global copy");
        // Jeśli nie znaleziono zaznaczenia, spróbuj globalnej akcji kopiowania
        return performGlobalAction(16); // 16 = GLOBAL_ACTION_COPY
    }

    private AccessibilityNodeInfo findSelectedNode(AccessibilityNodeInfo root) {
        if (root.isFocused() && root.getText() != null && !root.getText().toString().trim().isEmpty()) {
            return root;
        }

        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child != null) {
                if (child.isFocused() && child.getText() != null && !child.getText().toString().trim().isEmpty()) {
                    return child;
                }
                
                AccessibilityNodeInfo result = findSelectedNode(child);
                if (result != null) {
                    return result;
                }
                child.recycle();
            }
        }
        return null;
    }
}
