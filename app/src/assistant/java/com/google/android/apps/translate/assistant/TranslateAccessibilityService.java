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

public class TranslateAccessibilityService extends AccessibilityService {

    private static final String TAG = "GOTranslate";
    private static final long SESSION_TIMEOUT_MS = 5000; // 5 sekund timeout
    private static final long DEBOUNCE_DELAY_MS = 300; // Zapobiegaj wielokrotnemu uruchomieniu

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isAssistantSession = false;
    private long lastEventTime = 0;
    private Runnable timeoutRunnable = this::endSession;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "TranslateAccessibilityService created");
        
        // Maksymalna optymalizacja - minimalne eventy
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED; // TYLKO zmiana zaznaczenia
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        config.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        config.notificationTimeout = 100; // Krótki timeout
        config.packageNames = new String[]{
            "com.onyx.neverland.alreaderpro", // AlReader
            "org.mozilla.firefox", // Firefox
            "com.android.chrome", // Chrome
            "com.google.android.apps.docs" // Docs
        }; // TYLKO wybrane aplikacje
        
        setServiceInfo(config);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isAssistantSession) {
            return; // Działamy tylko podczas sesji asystenta
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEventTime < DEBOUNCE_DELAY_MS) {
            return; // Zapobiegaj spamowaniu eventami
        }
        lastEventTime = currentTime;

        Log.d(TAG, "Accessibility event: " + event.getEventType() + ", package: " + event.getPackageName());

        // Restart timeout
        handler.removeCallbacks(timeoutRunnable);
        handler.postDelayed(timeoutRunnable, SESSION_TIMEOUT_MS);

        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            processTextSelection(event);
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "TranslateAccessibilityService interrupted");
        endSession();
    }

    /**
     * Uruchamia sesję asystenta - wywoływane z VoiceInteractionService
     */
    public void startAssistantSession() {
        Log.d(TAG, "Starting assistant session");
        isAssistantSession = true;
        lastEventTime = System.currentTimeMillis();
        
        // Timeout na wypadek, gdyby tekst nie został znaleziony
        handler.removeCallbacks(timeoutRunnable);
        handler.postDelayed(timeoutRunnable, SESSION_TIMEOUT_MS);
    }

    /**
     * Kończy sesję asystenta
     */
    private void endSession() {
        Log.d(TAG, "Ending assistant session");
        isAssistantSession = false;
        handler.removeCallbacks(timeoutRunnable);
    }

    private void processTextSelection(AccessibilityEvent event) {
        try {
            String selectedText = findSelectedText(event);
            if (!TextUtils.isEmpty(selectedText)) {
                Log.d(TAG, "Found selected text: " + selectedText);
                redirectToTranslateActivity(selectedText);
                endSession(); // Zakończ sesję po znalezieniu tekstu
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing text selection", e);
        }
    }

    private String findSelectedText(AccessibilityEvent event) {
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return null;
        }

        try {
            // Szukaj zaznaczonego tekstu w drzewie dostępności
            return findSelectedTextInNode(source);
        } finally {
            source.recycle();
        }
    }

    private String findSelectedTextInNode(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }

        try {
            // Sprawdź czy node ma zaznaczony tekst
            CharSequence text = node.getText();
            if (text != null) {
                int start = node.getTextSelectionStart();
                int end = node.getTextSelectionEnd();
                
                if (start >= 0 && end > start && end <= text.length()) {
                    String selected = text.subSequence(start, end).toString().trim();
                    if (!TextUtils.isEmpty(selected) && selected.length() > 1) { // Minimum 2 znaki
                        Log.d(TAG, "Text selection found: " + selected);
                        return selected;
                    }
                }
            }

            // Rekurencyjnie przeszukaj dzieci (ogranicz głębokość dla wydajności)
            for (int i = 0; i < node.getChildCount() && i < 10; i++) { // Max 10 dzieci
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    String result = findSelectedTextInNode(child);
                    if (result != null) {
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error finding selected text in node", e);
        }
        
        return null;
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
            Log.d(TAG, "Successfully redirected to TranslateActivity");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start TranslateActivity", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "TranslateAccessibilityService destroyed");
        handler.removeCallbacks(timeoutRunnable);
    }
}
