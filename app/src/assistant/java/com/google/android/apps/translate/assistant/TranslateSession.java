package com.google.android.apps.translate.assistant;

import android.app.assist.AssistStructure;
import android.app.assist.AssistStructure.ViewNode;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.text.TextUtils;

public class TranslateSession extends VoiceInteractionSession {

    public TranslateSession(VoiceAssistantService service) {
        super(service);
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, android.app.assist.AssistContent content) {
        String selectedText = extractSelectedText(structure);
        if (!TextUtils.isEmpty(selectedText)) {
            redirectToTranslator(selectedText);
        }
        // Jeśli nie ma tekstu → nic nie robimy, po prostu finish()
        finish();
    }

    private String extractSelectedText(AssistStructure structure) {
        if (structure == null) return null;

        for (int i = 0; i < structure.getWindowNodeCount(); i++) {
            AssistStructure.WindowNode window = structure.getWindowNodeAt(i);
            ViewNode root = window.getRootViewNode();
            String text = traverse(root);
            if (!TextUtils.isEmpty(text)) return text;
        }
        return null;
    }

    private String traverse(ViewNode node) {
        if (node == null) return null;

        // Sprawdź, czy jest zaznaczenie
        if (node.getText() != null
            && node.getTextSelectionStart() >= 0
            && node.getTextSelectionEnd() > node.getTextSelectionStart()) {

            return node.getText()
                .subSequence(node.getTextSelectionStart(), node.getTextSelectionEnd())
                .toString();
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            String found = traverse(node.getChildAt(i));
            if (found != null) return found;
        }
        return null;
    }

    private void redirectToTranslator(String text) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
            "dev.davidv.translator",
            "dev.davidv.translator.ProcessTextActivity"
        ));
        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }
}
