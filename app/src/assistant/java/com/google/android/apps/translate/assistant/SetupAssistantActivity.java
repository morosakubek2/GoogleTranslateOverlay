package com.google.android.apps.translate.assistant;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.os.Bundle;

public class SetupAssistantActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Otwórz ustawienia, by użytkownik mógł ustawić jako domyślny asystent
        startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
        finish();
    }
}
