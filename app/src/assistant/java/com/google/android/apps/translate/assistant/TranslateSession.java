package com.google.android.apps.translate.assistant;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.assist.AssistStructure;
import android.app.assist.AssistStructure.ViewNode;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.text.TextUtils;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class TranslateSession extends VoiceInteractionSession {

    private static final String TAG = "TranslateSession";
    private static final String OFFLINE_PACKAGE = "dev.davidv.translator";
    private static final String OFFLINE_ACTIVITY = "dev.davidv.translator.ProcessTextActivity";
    private static final String CHANNEL_ID = "translate_assistant";

    public TranslateSession(Context context) {
        super(context);
        createNotificationChannel();
    }

    @Override
    public void onShow(Bundle args, int showFlags) {
        super.onShow(args, showFlags);
        Log.d(TAG, "onShow called with flags: " + showFlags);
        showNotification("Asystent uruchomiony", "Sprawdzam zaznaczony tekst...");
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, android.app.assist.AssistContent content) {
        Log.d(TAG, "onHandleAssist called");
        
        // Logowanie struktury
        if (structure != null) {
            Log.d(TAG, "AssistStructure windows: " + structure.getWindowNodeCount());
        } else {
            Log.d(TAG, "AssistStructure is null!");
            showNotification("Błąd", "Brak dostępu do struktury ekranu");
        }
        
        String selectedText = extractSelectedText(structure);
        
        if (!TextUtils.isEmpty(selectedText)) {
            Log.d(TAG, "Selected text found: " + selectedText);
            showNotification("Tekst znaleziony!", "Wysyłam do offline-translator: " + selectedText);
            redirectToOffline(selectedText);
        } else {
            Log.d(TAG, "No text selected");
            showNotification("Brak tekstu", "Nie znaleziono zaznaczonego tekstu");
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
        if (node.getText() != null) {
            int start = node.getTextSelectionStart();
            int end = node.getTextSelectionEnd();
            
            Log.d(TAG, "Node text: " + node.getText() + ", selection: " + start + "-" + end);
            
            if (start >= 0 && end > start) {
                CharSequence text = node.getText();
                return text.subSequence(start, end).toString();
            }
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
            Intent intent = new Intent(Intent.ACTION_PROCESS_TEXT);
            intent.setComponent(new ComponentName(OFFLINE_PACKAGE, OFFLINE_ACTIVITY));
            intent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
            intent.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            getContext().startActivity(intent);
            Log.d(TAG, "Redirected to offline translator");
            showNotification("Sukces!", "Tekst wysłany: " + text);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start offline translator", e);
            showNotification("Błąd!", "Nie udało się uruchomić offline-translator: " + e.getMessage());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Translate Assistant",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Powiadomienia asystenta tłumaczenia");
            
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(String title, String message) {
        try {
            NotificationManager notificationManager = 
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager == null) return;

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setTimeoutAfter(3000); // Auto-dismiss po 3 sekundach

            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
            Log.d(TAG, "Notification shown: " + title + " - " + message);
        } catch (Exception e) {
            Log.e(TAG, "Failed to show notification", e);
        }
    }
}
