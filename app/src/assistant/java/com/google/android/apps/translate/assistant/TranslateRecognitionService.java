package com.google.android.apps.translate.assistant;

import android.speech.RecognitionService;
import android.util.Log;

public class TranslateRecognitionService extends RecognitionService {
    private static final String TAG = "GOTr";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "TranslateRecognitionService created");
    }

    @Override
    protected void onStartListening(android.speech.RecognitionListener listener) {
        Log.d(TAG, "onStartListening - not implemented for translation overlay");
        // Nie używamy rozpoznawania mowy, więc nic nie robimy
    }

    @Override
    protected void onStopListening(android.speech.RecognitionListener listener) {
        Log.d(TAG, "onStopListening - not implemented for translation overlay");
        // Nie używamy rozpoznawania mowy, więc nic nie robimy
    }

    @Override
    protected void onCancel(android.speech.RecognitionListener listener) {
        Log.d(TAG, "onCancel - not implemented for translation overlay");
        // Nie używamy rozpoznawania mowy, więc nic nie robimy
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "TranslateRecognitionService destroyed");
    }
}
