package com.google.android.apps.translate.assistant;

import android.content.Intent;
import android.speech.RecognitionService;
import android.util.Log;

public class TranslateRecognitionService extends RecognitionService {
    private static final String TAG = "GOTr";

    @Override
    protected void onStartListening(Intent intent, RecognitionService.Callback callback) {
        Log.d(TAG, "TranslateRecognitionService: onStartListening");
        // Pusta implementacja - nie używamy rozpoznawania mowy
        callback.readyForSpeech(null);
    }

    @Override
    protected void onStopListening(RecognitionService.Callback callback) {
        Log.d(TAG, "TranslateRecognitionService: onStopListening");
        // Pusta implementacja
    }

    @Override
    protected void onCancel(RecognitionService.Callback callback) {
        Log.d(TAG, "TranslateRecognitionService: onCancel");
        // Pusta implementacja
    }
}
