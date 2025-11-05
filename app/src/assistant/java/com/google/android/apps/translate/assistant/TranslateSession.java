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

    private static final String TAG = "TranslateSession";
    private static final String OFFLINE_PACKAGE = "dev.davidv.translator";
    private static final String OFFLINE_ACTIVITY = "dev.davidv.translator.ProcessTextActivity";

    public TranslateSession(Context context) {
        super(context);
    }

    @Override
    public void onShow(Bundle args, int showFlags) {
        super.onShow(args, showFlags);
        Log.d(TAG, "onShow called");
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, android.app.assist.AssistContent content) {
        Log.d(TAG, "onHandleAssist called");
        
        String selectedText = extractSelectedText(structure);
        
        if (!TextUtils.isEmpty(selectedText)) {
            Log.d(TAG, "Selected text found: " + selectedText);
            redirectToOffline(selectedText);
        } else {
            Log.d(TAG, "No text selected");
        }
        
        // Zawsze kończymy sesję
        hide();
        finish();
    }

    private String extractSelectedText(AssistStructure structure) {
        if (structure == null) {
            Log.d(TAG, "AssistStructure is null");
            return null;
        }

        for (int i = 0; i < structure.getWindowNodeCount(); i++) {
            AssistStructure.WindowNode window = structure.getWindowNodeAt(i);
            ViewNode root = window.getRootViewNode();
            String text = traverseNode(root);
            if (text != null) return text;
        }
        return null;
    }

    private String traverseNode(ViewNode node) {
        if (node == null) return null;

        // Sprawdzamy zaznaczony tekst
        if (node.getText() != null && 
            node.getTextSelectionStart() >= 0 && 
            node.getTextSelectionEnd() > node.getTextSelectionStart()) {
            
            CharSequence text = node.getText();
            int start = node.getTextSelectionStart();
            int end = node.getTextSelectionEnd();
            
            return text.subSequence(start, end).toString();
        }

        // Rekurencyjnie sprawdzamy dzieci
        for (int i = 0; i < node.getChildCount(); i++) {
            String text = traverseNode(node.getChildAt(i));
            if (text != null) return text;
        }
        return null;
    }

    private void redirectToOffline(String text) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(OFFLINE_PACKAGE, OFFLINE_ACTIVITY));
            intent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            getContext().startActivity(intent);
            Log.d(TAG, "Redirected to offline translator");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start offline translator", e);
        }
    }
}
