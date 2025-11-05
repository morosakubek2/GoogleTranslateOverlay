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
        
        startAccessibilitySession();
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, android.app.assist.AssistContent content) {
        Log.d("GOTr", "=== ASSIST START ===");
        
        try {
            if (content != null) {
                Log.d("GOTr", "Checking AssistContent...");
                
                Intent intent = content.getIntent();
                if (intent != null) {
                    Log.d("GOTr", "AssistContent Intent: " + intent.getAction());
                    if (intent.hasExtra(Intent.EXTRA_PROCESS_TEXT)) {
                        String processText = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
                        if (!TextUtils.isEmpty(processText)) {
                            Log.d("GOTr", "FOUND TEXT in AssistContent EXTRA_PROCESS_TEXT: " + processText);
                            redirectToTranslateActivity(processText);
                            finish();
                            return;
                        }
                    }
                }
            }

            if (data != null) {
                Log.d("GOTr", "Checking Bundle data...");
                
                String text = data.getString(Intent.EXTRA_PROCESS_TEXT);
                if (!TextUtils.isEmpty(text)) {
                    Log.d("GOTr", "FOUND TEXT in Bundle EXTRA_PROCESS_TEXT: " + text);
                    redirectToTranslateActivity(text);
                    finish();
                    return;
                }
            }

            Log.d("GOTr", "Checking AssistStructure...");
            String selectedText = extractSelectedText(structure);
            
            if (!TextUtils.isEmpty(selectedText)) {
                Log.d("GOTr", "SELECTED TEXT from structure: " + selectedText);
                redirectToTranslateActivity(selectedText);
            } else {
                Log.d("GOTr", "No text found via Assist API - waiting for Accessibility Service");
            }
            
        } catch (Exception e) {
            Log.e("GOTr", "Error in onHandleAssist", e);
        }
    }

    private void startAccessibilitySession() {
        Log.d("GOTr", "Starting accessibility session from TranslateSession");
        Intent serviceIntent = new Intent(getContext(), TranslateAccessibilityService.class);
        serviceIntent.setAction("START_SESSION");
        getContext().startService(serviceIntent);
    }

    private String extractSelectedText(AssistStructure structure) {
        if (structure == null) {
            Log.w("GOTr", "extractSelectedText: structure is null");
            return null;
        }

        for (int i = 0; i < structure.getWindowNodeCount(); i++) {
            AssistStructure.WindowNode window = structure.getWindowNodeAt(i);
            ViewNode root = window.getRootViewNode();
            String text = traverseNode(root);
            if (text != null) {
                Log.d("GOTr", "Found text in window " + i);
                return text;
            }
        }
        Log.d("GOTr", "No selected text found in any window");
        return null;
    }

    private String traverseNode(ViewNode node) {
        if (node == null) return null;

        CharSequence nodeText = node.getText();
        if (nodeText != null) {
            int start = node.getTextSelectionStart();
            int end = node.getTextSelectionEnd();
            
            if (start >= 0 && end > start && end <= nodeText.length()) {
                String selected = nodeText.subSequence(start, end).toString();
                Log.d("GOTr", "Found selection: " + selected);
                return selected;
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
        Log.d("GOTr", "Successfully redirected to TranslateActivity with text: " + text);
        finish();
    }

    @Override
    public void onHide() {
        super.onHide();
        Log.d("GOTr", "TranslateSession hidden");
        finish();
    }
}
