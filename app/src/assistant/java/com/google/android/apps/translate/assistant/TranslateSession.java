package com.google.android.apps.translate.assistant;

import android.app.assist.AssistStructure;
import android.app.assist.AssistStructure.ViewNode;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.text.TextUtils;
import android.util.Log;

public class TranslateSession extends VoiceInteractionSession {

    private ClipboardManager clipboardManager;
    private String originalClipboardText = "";

    public TranslateSession(Context context) {
        super(context);
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
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
        
        // 1. Zapisz oryginalny schowek
        saveOriginalClipboard();
        
        // 2. Spróbuj znaleźć tekst w standardowych miejscach
        String text = findTextInAssistData(data, structure, content);
        
        if (!TextUtils.isEmpty(text)) {
            Log.d("GOTr", "Found text in assist data: " + text);
            redirectToTranslateActivity(text);
            restoreOriginalClipboard();
            finish();
            return;
        }

        // 3. Jeśli nie znaleziono tekstu, wywołaj kopiowanie
        Log.d("GOTr", "No text found - triggering copy action");
        triggerCopyAndProcess();
        
        finish();
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

    private void triggerCopyAndProcess() {
        try {
            // Wyślij akcję kopiowania
            performGlobalAction(GLOBAL_ACTION_COPY);
            Log.d("GOTr", "Copy action triggered");
            
            // Poczekaj chwilę na skopiowanie tekstu
            new Thread(() -> {
                try {
                    Thread.sleep(500); // Czekaj 500ms na skopiowanie
                    
                    // Sprawdź schowek w wątku głównym
                    getContext().getMainExecutor().execute(this::checkClipboardForText);
                    
                } catch (InterruptedException e) {
                    Log.e("GOTr", "Thread interrupted", e);
                    restoreOriginalClipboard();
                }
            }).start();
            
        } catch (Exception e) {
            Log.e("GOTr", "Failed to trigger copy action", e);
            restoreOriginalClipboard();
        }
    }

    private void checkClipboardForText() {
        if (clipboardManager == null || !clipboardManager.hasPrimaryClip()) {
            Log.d("GOTr", "Clipboard is empty");
            restoreOriginalClipboard();
            return;
        }

        ClipData clip = clipboardManager.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            CharSequence text = clip.getItemAt(0).getText();
            if (!TextUtils.isEmpty(text)) {
                String selectedText = text.toString().trim();
                
                // Sprawdź czy tekst się zmienił (czy udało się skopiować nowy tekst)
                if (!selectedText.equals(originalClipboardText) && selectedText.length() > 1) {
                    Log.d("GOTr", "Found text from clipboard: " + selectedText);
                    redirectToTranslateActivity(selectedText);
                    return;
                }
            }
        }
        
        Log.d("GOTr", "No new text in clipboard");
        restoreOriginalClipboard();
    }

    private void saveOriginalClipboard() {
        if (clipboardManager != null && clipboardManager.hasPrimaryClip()) {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (text != null) {
                    originalClipboardText = text.toString();
                    Log.d("GOTr", "Saved original clipboard: " + originalClipboardText);
                }
            }
        }
    }

    private void restoreOriginalClipboard() {
        if (clipboardManager != null && !TextUtils.isEmpty(originalClipboardText)) {
            ClipData clip = ClipData.newPlainText("original", originalClipboardText);
            clipboardManager.setPrimaryClip(clip);
            Log.d("GOTr", "Restored original clipboard");
        }
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
        try {
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
        } catch (Exception e) {
            Log.e("GOTr", "Failed to start TranslateActivity", e);
        } finally {
            restoreOriginalClipboard();
        }
    }

    @Override
    public void onHide() {
        super.onHide();
        Log.d("GOTr", "TranslateSession hidden");
    }
}
