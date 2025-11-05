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
        Log.d("GOTranslate", "TranslateSession created");
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, android.app.assist.AssistContent content) {
        Log.d("GOTranslate", "=== ASSIST START ===");
        
        String selectedText = extractSelectedText(structure);
        
        if (!TextUtils.isEmpty(selectedText)) {
            Log.d("GOTranslate", "SELECTED TEXT: " + selectedText);
            redirectToTranslateActivity(selectedText);
        } else {
            Log.d("GOTranslate", "No text selected");
        }
        
        Log.d("GOTranslate", "=== ASSIST END ===");
        finish();
    }

    private String extractSelectedText(AssistStructure structure) {
        if (structure == null) {
            Log.w("GOTranslate", "extractSelectedText: structure is null");
            return null;
        }

        for (int i = 0; i < structure.getWindowNodeCount(); i++) {
            AssistStructure.WindowNode window = structure.getWindowNodeAt(i);
            ViewNode root = window.getRootViewNode();
            String text = traverseNode(root);
            if (text != null) {
                Log.d("GOTranslate", "Found text in window " + i);
                return text;
            }
        }
        Log.d("GOTranslate", "No selected text found in any window");
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
                Log.d("GOTranslate", "Found selection: " + selected);
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
        Log.d("GOTranslate", "Successfully redirected to TranslateActivity with text: " + text);
    }
}
