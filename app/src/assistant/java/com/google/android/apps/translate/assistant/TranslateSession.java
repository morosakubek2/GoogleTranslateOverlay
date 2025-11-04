package com.google.android.apps.translate.assistant;

import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.app.assist.AssistStructure.ViewNode;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.text.TextUtils;

public class TranslateSession extends VoiceInteractionSession {

    public TranslateSession(VoiceInteractionService service) {
        super(service);
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, AssistContent content) {
        String selectedText = extractSelectedText(structure);
        if (!TextUtils.isEmpty(selectedText)) {
            redirectToOffline(selectedText);
        } else {
            finish(); // Zaprzestaj, jeśli nie ma zaznaczonego tekstu
        }
    }

    private String extractSelectedText(AssistStructure structure) {
        if (structure == null) return null;

        int windowCount = structure.getWindowNodeCount();
        for (int i = 0; i < windowCount; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            ViewNode root = windowNode.getRootViewNode();
            String text = traverseNode(root);
            if (!TextUtils.isEmpty(text)) return text;
        }
        return null;
    }

    private String traverseNode(ViewNode node) {
        if (node == null) return null;

        if (node.getText() != null && node.getTextSelectionStart() >= 0 && node.getTextSelectionEnd() > node.getTextSelectionStart()) {
            return node.getText().subSequence(node.getTextSelectionStart(), node.getTextSelectionEnd()).toString();
        }

        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            String text = traverseNode(node.getChildAt(i));
            if (!TextUtils.isEmpty(text)) return text;
        }

        return null;
    }

    private void redirectToOffline(String text) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("dev.davidv.translator", "dev.davidv.translator.ProcessTextActivity"));
        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
        finish();
    }
}
