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
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.List;

public class TranslateAccessibilityService extends AccessibilityService {

    private static final long SESSION_TIMEOUT_MS = 10000;
    private static final long DEBOUNCE_DELAY_MS = 200;
    private static final long RESUME_DELAY_MS = 300;

    private static TranslateAccessibilityService instance;

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isAssistantSession = false;
    private long lastEventTime = 0;
    private Runnable timeoutRunnable = this::endSession;
    private Runnable checkSelectionRunnable = this::checkCurrentSelection;
    
    private ClipboardManager clipboardManager;
    private String originalClipboardText = "";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        
        Log.d("GOTr", "TranslateAccessibilityService connected");
        
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
        Log.d("GOTr", "Service configured with full window access");
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
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isAssistantSession) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEventTime < DEBOUNCE_DELAY_MS) {
            return;
        }
        lastEventTime = currentTime;

        Log.d("GOTr", "Accessibility event: " + event.getEventType() + ", package: " + event.getPackageName());

        handler.removeCallbacks(timeoutRunnable);
        handler.postDelayed(timeoutRunnable, SESSION_TIMEOUT_MS);

        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            processTextSelection(event);
        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED 
                || event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            Log.d("GOTr", "Window changed – checking selection and ActionMode");
            handler.removeCallbacks(checkSelectionRunnable);
            handler.postDelayed(checkSelectionRunnable, RESUME_DELAY_MS);
            
            // Dodatkowo sprawdź czy pojawiło się menu ActionMode
            checkForActionMode();
        }
    }

    @Override
    public void onInterrupt() {
        Log.d("GOTr", "Service interrupted");
        endSession();
    }

    private void startAssistantSession() {
        Log.d("GOTr", "Starting assistant session");
        isAssistantSession = true;
        lastEventTime = System.currentTimeMillis();
        
        // Zapisz oryginalną zawartość schowka
        saveOriginalClipboard();
        
        handler.removeCallbacks(checkSelectionRunnable);
        handler.post(checkSelectionRunnable);
        
        handler.removeCallbacks(timeoutRunnable);
        handler.postDelayed(timeoutRunnable, SESSION_TIMEOUT_MS);
    }

    private void endSession() {
        Log.d("GOTr", "Ending assistant session");
        isAssistantSession = false;
        handler.removeCallbacks(timeoutRunnable);
        handler.removeCallbacks(checkSelectionRunnable);
        
        // Przywróć oryginalny schowek
        restoreOriginalClipboard();
    }

    private void saveOriginalClipboard() {
        if (clipboardManager != null && clipboardManager.hasPrimaryClip()) {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (text != null) {
                    originalClipboardText = text.toString();
                }
            }
        }
    }

    private void restoreOriginalClipboard() {
        if (clipboardManager != null && !TextUtils.isEmpty(originalClipboardText)) {
            ClipData clip = ClipData.newPlainText("original", originalClipboardText);
            clipboardManager.setPrimaryClip(clip);
        }
    }

    private void checkCurrentSelection() {
        if (!isAssistantSession) return;

        // Metoda 1: Standardowe wyszukiwanie zaznaczenia
        String selectedText = findSelectedTextInWindows();
        if (!TextUtils.isEmpty(selectedText)) {
            handleSelectedText(selectedText);
            return;
        }

        // Metoda 2: Sprawdź czy jest aktywny ActionMode
        checkForActionMode();
    }

    private String findSelectedTextInWindows() {
        List<AccessibilityWindowInfo> windows = getWindows();
        if (windows == null || windows.isEmpty()) {
            Log.d("GOTr", "No windows found");
            return null;
        }

        Log.d("GOTr", "Found " + windows.size() + " windows");

        for (AccessibilityWindowInfo window : windows) {
            if (window == null) continue;
            
            AccessibilityNodeInfo root = window.getRoot();
            if (root == null) {
                continue;
            }

            Log.d("GOTr", "Checking window: " + window.getTitle() + ", package: " + root.getPackageName());
            String selectedText = findSelectedTextInNode(root);
            root.recycle();
            
            if (!TextUtils.isEmpty(selectedText)) {
                return selectedText;
            }
        }

        return null;
    }

    private void checkForActionMode() {
        Log.d("GOTr", "Checking for ActionMode...");
        
        List<AccessibilityWindowInfo> windows = getWindows();
        if (windows == null) return;

        for (AccessibilityWindowInfo window : windows) {
            if (window == null) continue;
            
            // Szukaj okna typu TYPE_SYSTEM_DIALOG - to często jest ActionMode
            if (window.getType() == AccessibilityWindowInfo.TYPE_SYSTEM_DIALOG) {
                Log.d("GOTr", "Found system dialog window - might be ActionMode");
                AccessibilityNodeInfo root = window.getRoot();
                if (root != null) {
                    findAndClickCopyButton(root);
                    root.recycle();
                }
            }
            
            // Szukaj przycisków kopiowania w innych oknach
            AccessibilityNodeInfo root = window.getRoot();
            if (root != null) {
                findAndClickCopyButton(root);
                root.recycle();
            }
        }
    }

    private void findAndClickCopyButton(AccessibilityNodeInfo root) {
        // Szukaj przycisku "Kopiuj" lub "Copy" w całym drzewie
        List<AccessibilityNodeInfo> copyButtons = root.findAccessibilityNodeInfosByText("Kopiuj");
        if (copyButtons.isEmpty()) {
            copyButtons = root.findAccessibilityNodeInfosByText("Copy");
        }
        
        for (AccessibilityNodeInfo button : copyButtons) {
            Log.d("GOTr", "Found copy button: " + button.getText());
            if (button.isClickable()) {
                Log.d("GOTr", "Clicking copy button");
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                
                // Poczekaj chwilę i sprawdź schowek
                handler.postDelayed(this::checkClipboardForText, 500);
                break;
            }
            button.recycle();
        }
    }

    private void checkClipboardForText() {
        if (clipboardManager == null || !clipboardManager.hasPrimaryClip()) {
            return;
        }

        ClipData clip = clipboardManager.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            CharSequence text = clip.getItemAt(0).getText();
            if (!TextUtils.isEmpty(text)) {
                String selectedText = text.toString().trim();
                // Sprawdź czy to nie jest ten sam tekst co oryginalny schowek
                if (!selectedText.equals(originalClipboardText) && selectedText.length() > 1) {
                    Log.d("GOTr", "Found text from clipboard: " + selectedText);
                    handleSelectedText(selectedText);
                }
            }
        }
    }

    private void processTextSelection(AccessibilityEvent event) {
        int start = event.getFromIndex();
        int end = event.getToIndex();
        Log.d("GOTr", "TEXT SELECTION CHANGED: start=" + start + ", end=" + end);
        
        String selectedText = extractSelectionFromEvent(event);
        if (TextUtils.isEmpty(selectedText)) {
            selectedText = findSelectedText(event);
        }
        
        if (!TextUtils.isEmpty(selectedText)) {
            Log.d("GOTr", "Found text from selection event: " + selectedText);
            handleSelectedText(selectedText);
        }
    }

    private String extractSelectionFromEvent(AccessibilityEvent event) {
        if (event.getText() != null && !event.getText().isEmpty()) {
            CharSequence text = event.getText().get(0);
            int start = event.getFromIndex();
            int end = event.getToIndex();
            
            if (start >= 0 && end > start && end <= text.length()) {
                String selected = text.subSequence(start, end).toString().trim();
                if (!TextUtils.isEmpty(selected) && selected.length() > 1) {
                    return selected;
                }
            }
        }
        return null;
    }

    private String findSelectedText(AccessibilityEvent event) {
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return null;
        }

        String result = findSelectedTextInNode(source);
        source.recycle();
        return result;
    }

    private String findSelectedTextInNode(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }

        // Sprawdź zaznaczenie w bieżącym węźle
        CharSequence text = node.getText();
        if (text != null) {
            int start = node.getTextSelectionStart();
            int end = node.getTextSelectionEnd();
            
            Log.d("GOTr", "Node text: '" + text + "', selection: " + start + "-" + end);
            
            if (start >= 0 && end > start && end <= text.length()) {
                String selected = text.subSequence(start, end).toString().trim();
                if (!TextUtils.isEmpty(selected) && selected.length() > 1) {
                    Log.d("GOTr", "Found selection in node: " + selected);
                    return selected;
                }
            }
        }

        // Przeszukaj dzieci
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                String result = findSelectedTextInNode(child);
                child.recycle();
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }

    private void handleSelectedText(String text) {
        if (!TextUtils.isEmpty(text)) {
            redirectToTranslateActivity(text);
            endSession();
        }
    }

    private void redirectToTranslateActivity(String text) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setComponent(new ComponentName(
                getPackageName(),
                "com.google.android.apps.translate.TranslateActivity"
            ));
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, text);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            
            startActivity(intent);
            Log.d("GOTr", "Successfully redirected to translator with text: " + text);
        } catch (Exception e) {
            Log.e("GOTr", "Failed to start TranslateActivity", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d("GOTr", "Service destroyed");
        handler.removeCallbacks(timeoutRunnable);
        handler.removeCallbacks(checkSelectionRunnable);
    }
}
