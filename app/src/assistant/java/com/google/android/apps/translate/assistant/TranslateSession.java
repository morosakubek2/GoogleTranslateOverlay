package com.google.android.apps.translate.assistant;

import android.app.assist.AssistStructure;
import android.app.assist.AssistStructure.ViewNode;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.text.TextUtils;
import android.util.Log;

public class TranslateSession extends VoiceInteractionSession {

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
        
        // 1. Sprawdź AssistContent
        if (content != null && content.getIntent() != null) {
            Intent intent = content.getIntent();
            String text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
            if (!TextUtils.isEmpty(text)) {
                Log.d("GOTr", "Found text in AssistContent: " + text);
                redirectToTranslateActivity(text);
                finish();
                return;
            }
        }

        // 2. Sprawdź Bundle
        if (data != null) {
            String text = data.getString(Intent.EXTRA_PROCESS_TEXT);
            if (!TextUtils.isEmpty(text)) {
                Log.d("GOTr", "Found text in Bundle: " + text);
                redirectToTranslateActivity(text);
                finish();
                return;
            }
        }

        // 3. Sprawdź AssistStructure
        String selectedText = extractSelectedText(structure);
        if (!TextUtils.isEmpty(selectedText)) {
            Log.d("GOTr", "Found text in AssistStructure: " + selectedText);
            redirectToTranslateActivity(selectedText);
            finish();
            return;
        }

        // 4. Fallback - uruchom PROCESS_TEXT
        Log.d("GOTr", "No text found - starting PROCESS_TEXT");
        startProcessText();
        finish();
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

    private void startProcessText() {
        Intent processTextIntent = new Intent(Intent.ACTION_PROCESS_TEXT);
        processTextIntent.setType("text/plain");
        processTextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        // Ustaw naszą aplikację jako handler
        processTextIntent.setComponent(new ComponentName(
            getContext().getPackageName(),
            "com.google.android.apps.translate.copydrop.gm3.TapToTranslateActivity"
        ));
        
        getContext().startActivity(processTextIntent);
        Log.d("GOTr", "Started PROCESS_TEXT");
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
        Log.d("GOTr", "Redirected to TranslateActivity");
    }

    @Override
    public void onHide() {
        super.onHide();
        Log.d("GOTr", "TranslateSession hidden");
    }
}
