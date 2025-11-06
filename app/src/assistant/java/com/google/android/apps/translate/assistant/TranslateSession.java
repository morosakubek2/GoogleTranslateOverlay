package com.google.android.apps.translate.assistant;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.util.Log;

public class TranslateSession extends VoiceInteractionSession {

    private ClipboardManager clipboardManager;
    private String originalClipboardContent;

    public TranslateSession(Context context) {
        super(context);
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        Log.d("GOTr", "TranslateSession created");
    }

    @Override
    public void onShow(Bundle args, int showFlags) {
        super.onShow(args, showFlags);
        Log.d("GOTr", "onShow called with flags: " + showFlags);
        
        // Zapisz oryginalną zawartość schowka
        saveOriginalClipboard();
    }

    @Override
    public void onHandleAssist(Bundle data, android.app.assist.AssistStructure structure, android.app.assist.AssistContent content) {
        Log.d("GOTr", "=== ASSIST START ===");
        
        Log.d("GOTr", "Triggering copy to clipboard");
        boolean copySuccess = performGlobalAction(GLOBAL_ACTION_COPY);
        
        if (copySuccess) {
            // Poczekaj chwilę na skopiowanie
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            if (hasNewTextInClipboard()) {
                Log.d("GOTr", "New text detected in clipboard - launching translation");
                launchTranslation();
            } else {
                Log.d("GOTr", "No new text in clipboard - nothing to translate");
                restoreOriginalClipboard();
            }
        } else {
            Log.d("GOTr", "Copy action failed");
        }
        
        finish();
    }

    private void saveOriginalClipboard() {
        if (clipboardManager.hasPrimaryClip()) {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (text != null) {
                    originalClipboardContent = text.toString();
                    Log.d("GOTr", "Saved original clipboard: " + originalClipboardContent);
                }
            }
        }
    }

    private boolean hasNewTextInClipboard() {
        if (!clipboardManager.hasPrimaryClip()) {
            return false;
        }

        ClipData clip = clipboardManager.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            return false;
        }

        CharSequence newText = clip.getItemAt(0).getText();
        if (newText == null) {
            return false;
        }

        String newTextStr = newText.toString().trim();
        
        // Sprawdź czy nowy tekst jest inny niż oryginalny i nie jest pusty
        boolean hasNewText = !newTextStr.equals(originalClipboardContent) && 
                           !newTextStr.isEmpty() && 
                           newTextStr.length() > 1;
        
        Log.d("GOTr", "Clipboard check - original: '" + originalClipboardContent + "', new: '" + newTextStr + "', hasNewText: " + hasNewText);
        
        return hasNewText;
    }

    private void restoreOriginalClipboard() {
        if (originalClipboardContent != null) {
            ClipData clip = ClipData.newPlainText("original", originalClipboardContent);
            clipboardManager.setPrimaryClip(clip);
            Log.d("GOTr", "Restored original clipboard content");
        }
    }

    private void launchTranslation() {
        Intent processTextIntent = new Intent(Intent.ACTION_PROCESS_TEXT);
        processTextIntent.setType("text/plain");
        processTextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        processTextIntent.setComponent(new ComponentName(
            getContext().getPackageName(),
            "com.google.android.apps.translate.copydrop.gm3.TapToTranslateActivity"
        ));
        
        getContext().startActivity(processTextIntent);
        Log.d("GOTr", "Launched translation");
    }

    @Override
    public void onHide() {
        super.onHide();
        Log.d("GOTr", "TranslateSession hidden");
    }
}
