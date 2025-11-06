package com.google.android.apps.translate.assistant;

import android.speech.RecognitionService;
import android.util.Log;

public class TranslateRecognitionService extends RecognitionService {
    private static final String TAG = "GOTr";

    @Override
    protected void onStartListening(RecognitionService.Callback callback) {
        Log.d(TAG, "TranslateRecognitionService: onStartListening");
        // Pusta implementacja - nie używamy rozpoznawania mowy
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

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "TranslateRecognitionService created");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "TranslateRecognitionService destroyed");
        super.onDestroy();
    }
}
