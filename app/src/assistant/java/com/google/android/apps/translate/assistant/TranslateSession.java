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

    public TranslateSession(Context context) {
        super(context);
    }

    @Override
    public void onShow(Bundle args, int showFlags) {
        super.onShow(args, showFlags);
        Log.d(TAG, "onShow called with flags: " + showFlags);
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, android.app.assist.AssistContent content) {
        Log.d(TAG, "onHandleAssist called");
        
        if (structure != null) {
            Log.d(TAG, "AssistStructure windows: " + structure.getWindowNodeCount());
        } else {
            Log.w(TAG, "AssistStructure is null!");
        }
        
        String selectedText = extractSelectedText(structure);
        
        if (!TextUtils.isEmpty(selectedText)) {
            Log.d(TAG, "Selected text found: " + selectedText);
            redirectToTranslateActivity(selectedText);
        } else {
            Log.d(TAG, "No text selected");
        }
        
        hide();
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
            if (text != null) return text;
        }
        return null;
    }

    private String traverseNode(ViewNode node) {
        if (node == null) return null;

        if (node.getText() != null) {
            int start = node.getTextSelectionStart();
            int end = node.getTextSelectionEnd();
            
            if (start >= 0 && end > start) {
                CharSequence text = node.getText();
                String selected = text.subSequence(start, end).toString();
                Log.d(TAG, "Found selection: " + selected);
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
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setComponent(new ComponentName(
                getContext().getPackageName(),
                "com.google.android.apps.translate.TranslateActivity"
            ));
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, text);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            getContext().startActivity(intent);
            Log.d(TAG, "Redirected to TranslateActivity with text: " + text);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start TranslateActivity", e);
        }
    }
}
