package com.google.android.apps.translate.assistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

public class SetupAssistantActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS);
        startActivity(intent);
        finish();
    }
}
