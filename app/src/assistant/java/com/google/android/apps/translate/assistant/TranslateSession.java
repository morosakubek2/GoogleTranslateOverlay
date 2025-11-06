package com.google.android.apps.translate.assistant;

import android.app.assist.AssistStructure;
import android.app.assist.AssistStructure.ViewNode;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.voice.VoiceInteractionSession;
import android.text.TextUtils;
import android.util.Log;

public class TranslateSession extends VoiceInteractionSession {

    private Handler handler = new Handler(Looper.getMainLooper());
    private String originalClipboardText = "";

    public TranslateSession(Context context) {
        super(context);
        Log.d("GOTr", "TranslateSession created");
    }

    @Override
    public void onShow(Bundle args, int showFlags) {
        super.onShow(args, showFlags);
        Log.d("GOTr", "onShow called with flags: " + showFlags);
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, android.app.assist.AssistContent content) {
        Log.d("GOTr", "=== ASSIST START ===");
        
        // 1. Spróbuj znaleźć tekst w standardowych miejscach
        String text = findTextInAssistData(data, structure, content);
        
        if (!TextUtils.isEmpty(text)) {
            Log.d("GOTr", "Found text in assist data: " + text);
            redirectToTranslateActivity(text);
            finish();
            return;
        }

        // 2. Jeśli nie znaleziono tekstu, użyj kopiowania + AccessibilityService do schowka
        Log.d("GOTr", "No text found - triggering copy + clipboard read");
        triggerCopyAndReadClipboard();
    }

    private String findTextInAssistData(Bundle data, AssistStructure structure, android.app.assist.AssistContent content) {
        // Sprawdź AssistContent
        if (content != null && content.getIntent() != null) {
            Intent intent = content.getIntent();
            String text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
            if (!TextUtils.isEmpty(text)) {
                return text;
            }
        }

        // Sprawdź Bundle
        if (data != null) {
            String text = data.getString(Intent.EXTRA_PROCESS_TEXT);
            if (!TextUtils.isEmpty(text)) {
                return text;
            }
        }

        // Sprawdź AssistStructure
        return extractSelectedText(structure);
    }

    private void triggerCopyAndReadClipboard() {
        // Najpierw zapisz oryginalny schowek przez AccessibilityService
        saveOriginalClipboard();
        
        // Wyślij akcję kopiowania
        boolean copySuccess = performGlobalAction(GLOBAL_ACTION_COPY);
        
        if (copySuccess) {
            Log.d("GOTr", "Copy action triggered successfully");
            
            // Poczekaj chwilę na skopiowanie tekstu, potem przeczytaj schowek
            handler.postDelayed(this::readClipboardAfterCopy, 500);
        } else {
            Log.e("GOTr", "Copy action failed");
            // Fallback: spróbuj bezpośrednio przez PROCESS_TEXT
            startProcessTextFallback();
            finish();
        }
    }

    private void saveOriginalClipboard() {
        ClipboardAccessibilityService.getClipboardText(new ClipboardAccessibilityService.ClipboardCallback() {
            @Override
            public void onClipboardText(String text) {
                originalClipboardText = text != null ? text : "";
                Log.d("GOTr", "Saved original clipboard: " + originalClipboardText);
            }
        });
    }

    private void readClipboardAfterCopy() {
        ClipboardAccessibilityService.getClipboardText(new ClipboardAccessibilityService.ClipboardCallback() {
            @Override
            public void onClipboardText(String text) {
                if (!TextUtils.isEmpty(text)) {
                    // Sprawdź czy tekst się zmienił (czy udało się skopiować nowy tekst)
                    if (!text.equals(originalClipboardText) && text.length() > 1) {
                        Log.d("GOTr", "Found new text from clipboard: " + text);
                        redirectToTranslateActivity(text);
                    } else {
                        Log.d("GOTr", "Clipboard text unchanged or too short - trying fallback");
                        startProcessTextFallback();
                    }
                } else {
                    Log.d("GOTr", "No text in clipboard - trying fallback");
                    startProcessTextFallback();
                }
                finish();
            }
        });
    }

    private void startProcessTextFallback() {
        Intent processTextIntent = new Intent(Intent.ACTION_PROCESS_TEXT);
        processTextIntent.setType("text/plain");
        processTextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        // Ustaw naszą aplikację jako handler
        processTextIntent.setComponent(new ComponentName(
            getContext().getPackageName(),
            "com.google.android.apps.translate.copydrop.gm3.TapToTranslateActivity"
        ));
        
        // Jeśli nie możemy uruchomić PROCESS_TEXT, to już nie mamy więcej opcji
        getContext().startActivity(processTextIntent);
        Log.d("GOTr", "Started PROCESS_TEXT fallback");
    }

    private String extractSelectedText(AssistStructure structure) {
        if (structure == null) {
            return null;
        }

        for (int i = 0; i < structure.getWindowNodeCount(); i++) {
            AssistStructure.WindowNode window = structure.getWindowNodeAt(i);
            ViewNode root = window.getRootViewNode();
            String text = traverseNode(root);
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    private String traverseNode(ViewNode node) {
        if (node == null) return null;

        CharSequence nodeText = node.getText();
        if (nodeText != null) {
            int start = node.getTextSelectionStart();
            int end = node.getTextSelectionEnd();
            
            if (start >= 0 && end > start && end <= nodeText.length()) {
                return nodeText.subSequence(start, end).toString();
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            String text = traverseNode(node.getChildAt(i));
            if (text != null) return text;
        }
        return null;
    }

    private void redirectToTranslateActivity(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setComponent(new ComponentName(
            getContext().getPackageName(),
            "com.google.android.apps.translate.TranslateActivity"
        ));
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        getContext().startActivity(intent);
        Log.d("GOTr", "Redirected to TranslateActivity with text: " + text);
    }

    @Override
    public void onHide() {
        super.onHide();
        Log.d("GOTr", "TranslateSession hidden");
    }
}
